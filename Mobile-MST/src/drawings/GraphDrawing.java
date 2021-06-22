package drawings;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JComponent;

public class GraphDrawing extends JComponent{
	
	private int size;
	private MovingPointSet2D vertices;
	private LinkedList<Integer>[] adjList;
	private LinkedList<Edge> edgeList;
	
	//contains differences to another graph drawing which can be visualized
	private LinkedList<Edge> additionalEdges;
	private LinkedList<Edge> missingEdges;
	
	//statistical data
	private int edgesVisitedGreedy;
	private int edgesVisitedOptimalSlow;
	private int edgesVisitedBranchAndBound;
	private int comparisonsCrossingCriterion;
	
	public GraphDrawing() {
		this.edgeList = new LinkedList<Edge>();
	}
	
	public GraphDrawing(MovingPointSet2D vertices) {
		this.vertices = vertices;
		this.size = vertices.getsize();
		this.edgeList = new LinkedList<Edge>();
		
		this.adjList = new LinkedList[this.size];
		//init neighbour entries for all vertices
		for(int i = 0; i < adjList.length; i++) {
			this.adjList[i] = new LinkedList<Integer>();
		}
		this.edgesVisitedGreedy = 0;
		this.edgesVisitedOptimalSlow = 0;
		this.edgesVisitedBranchAndBound = 0;
		this.comparisonsCrossingCriterion = 0;
	}
	
	public GraphDrawing(MovingPointSet2D vertices, LinkedList<Edge> edgeList) {
		this.vertices = vertices;
		this.size = vertices.getsize();
		this.edgeList = edgeList;
		
		this.adjList = new LinkedList[this.size];
		//init neighbour entries for all vertices
		for(int i = 0; i < adjList.length; i++) {
			this.adjList[i] = new LinkedList<Integer>();
		}
		this.edgesVisitedGreedy = 0;
		this.edgesVisitedOptimalSlow = 0;
		this.edgesVisitedBranchAndBound = 0;
	}
	
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		this.vertices.paint(g2d);
		if(!edgeList.isEmpty()) {
			for(Edge edge : edgeList) {
				edge.getPosition().paint(g2d);
			}
		}
		//paint edges that were found in the compared GraphDrawing but not in this one green
		if(!(missingEdges == null)) {
			for(Edge edge : missingEdges) {
				edge.getPosition().paintGreen(g2d);
			}
		}
		
		//paint edges that were not found in the compared GraphDrawing but that exist in this one red
		if(!(additionalEdges == null)) {
			for(Edge edge : additionalEdges) {
				edge.getPosition().paintRed(g2d);
			}
		}
	}	
	
	
	//adds all edges of a complete graph on the pointset
	public void completeGraph() {
		edgeList = new LinkedList<Edge>();
		LineSegment line;
		for(int i = 0; i < this.size - 1; i++) {
			for(int j = i+1; j < this.size; j++) {
				adjList[i].add(j);
				adjList[j].add(i);
				line = new LineSegment(vertices.getPoint(i), vertices.getPoint(j));
				edgeList.add(new Edge(i, j, line));
			}
		}
	}
	
	//adds all edges of a complete graph on the pointset that do not violate the point criterion
	public void completeGraphSetminusPK() {
		edgeList = new LinkedList<Edge>();
		LineSegment line;
		Edge edge;
		for(int i = 0; i < this.size - 1; i++) {
			for(int j = i+1; j < this.size; j++) {
				line = new LineSegment(vertices.getPoint(i), vertices.getPoint(j));
				edge = new Edge(i, j, line);
				if(!this.vertices.violatesPointCriterion(edge.getPosition())) {
					adjList[i].add(j);
					adjList[j].add(i);
					edgeList.add(edge);
				}
			}
		}
	}
	

	//Kruskal's algorithm on the initial position of the moving pointset
	public int kruskal() {
		
		this.completeGraph();

		/*
		// Uncomment to use swept area instead of initial length as weight
		for(Edge edge : edgeList) {
			edge.areaAsWeight();
		}
		*/
		
		this.edgesVisitedGreedy = 0;		
		//union-find
		int[] parents = new int[this.size];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
		}
		//output
		LinkedList<Edge> MST = new LinkedList<Edge>();
		
		
		Collections.sort(this.edgeList);
		
		boolean success = false;
		while(!edgeList.isEmpty()) {
			Edge nextEdge = edgeList.removeFirst();
			this.edgesVisitedGreedy += 1;
			
			int v = getRepresentative(parents, nextEdge.getSrc());
			int w = getRepresentative(parents, nextEdge.getDest());
			
			if(v != w) {
				MST.add(nextEdge);
				union(parents, v, w);
			}
			if (MST.size() >= this.size - 1) {
				success = true;
				break;
			}
		}
		
		if(!success) {
			System.out.println("Ran out of edges");
		}
		
		this.edgeList = MST;
		this.adjList = new LinkedList[this.size];
		for(int i = 0; i < adjList.length; i++) {
			this.adjList[i] = new LinkedList<Integer>();
		}
		int src;
		int dest;
		for(Edge edge : edgeList) {
			src = edge.getSrc();
			dest = edge.getDest();
			adjList[src].add(dest);
			adjList[dest].add(src);
		}
		return this.edgesVisitedGreedy;
	}
	
	//produces a crossing-stable MST on the moving pointset
	public int crossingStableMST() {
		
		this.completeGraphSetminusPK();

		/*
		// Uncomment to use swept area instead of initial length as weight
		for(Edge edge : edgeList) {
			edge.areaAsWeight();
		}
		*/
				
		this.edgesVisitedGreedy = 0;		
		//union-find
		int[] parents = new int[this.size];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
		}
		//output
		LinkedList<Edge> MST = new LinkedList<Edge>();
		
		Collections.sort(this.edgeList);
		
		boolean success = false;
		while(!edgeList.isEmpty()) {
			Edge nextEdge = edgeList.removeFirst();
			this.edgesVisitedGreedy += 1;
			
			int v = getRepresentative(parents, nextEdge.getSrc());
			int w = getRepresentative(parents, nextEdge.getDest());
			
			if(v != w) {
				MST.add(nextEdge);
				union(parents, v, w);
			}
			if (MST.size() >= this.size - 1) {
				success = true;
				break;
			}
		}
		
		if(!success) {
			System.out.println("Ran out of edges");
		}
		
		this.edgeList = MST;
		this.adjList = new LinkedList[this.size];
		for(int i = 0; i < adjList.length; i++) {
			this.adjList[i] = new LinkedList<Integer>();
		}
		int src;
		int dest;
		for(Edge edge : edgeList) {
			src = edge.getSrc();
			dest = edge.getDest();
			adjList[src].add(dest);
			adjList[dest].add(src);
		}
		return this.edgesVisitedGreedy;
	}
	
	
	//produces a cheap planar spanning tree on a moving pointset
	public int movingKruskal() {
		
		this.completeGraphSetminusPK();

		/*
		// Uncomment to use swept area instead of initial length as weight
		for(Edge edge : edgeList) {
			edge.areaAsWeight();
		}
		*/
		
		this.edgesVisitedGreedy = 0;		
		//union-find
		int[] parents = new int[this.size];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
		}
		//output
		LinkedList<Edge> MST = new LinkedList<Edge>();
		
		Collections.sort(this.edgeList);
		
		boolean success = false;
		while(!edgeList.isEmpty()) {
			Edge nextEdge = edgeList.removeFirst();
			this.edgesVisitedGreedy += 1;
			
			int v = getRepresentative(parents, nextEdge.getSrc());
			int w = getRepresentative(parents, nextEdge.getDest());
			
			if(v != w) {
				if(!isCrossing(nextEdge, MST)){
					MST.add(nextEdge);
					union(parents, v, w);
				}
			}
			if (MST.size() >= this.size - 1) {
				success = true;
				break;
			}
		}
		
		if(!success) {
			System.out.println("Ran out of edges");
		}
		
		this.edgeList = MST;
		this.adjList = new LinkedList[this.size];
		for(int i = 0; i < adjList.length; i++) {
			this.adjList[i] = new LinkedList<Integer>();
		}
		int src;
		int dest;
		for(Edge edge : edgeList) {
			src = edge.getSrc();
			dest = edge.getDest();
			adjList[src].add(dest);
			adjList[dest].add(src);
		}
		return this.edgesVisitedGreedy;
	}
	
	//brute Forces a planar minimal spanning tree on a moving pointset
	public int optimalSlow() {
		this.completeGraphSetminusPK();
		
		/*
		// Uncomment to use swept area instead of initial length as weight
		for(Edge edge : edgeList) {
			edge.areaAsWeight();
		}
		*/
		
		Collections.sort(this.edgeList);
		LinkedList<Edge> currentCandidates = new LinkedList<Edge>();
		for(Edge edge : this.edgeList) {
			currentCandidates.add(edge);
		}

		this.edgesVisitedOptimalSlow = 0;
		this.comparisonsCrossingCriterion = 0;
		
		//union-find
		int[] parents = new int[this.size];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
		}
		
		LinkedList<Edge> MST = new LinkedList<Edge>();
		LinkedList<Edge> deletedEdges = new LinkedList<Edge>();
		MST = optimalSlowRecursive(MST, currentCandidates, parents, deletedEdges);
		this.edgeList = MST;
		
		return this.edgesVisitedOptimalSlow;
	}
	
	//recursive procedure of Brute Force algorithm for planar minimal spanning tree on a moving pointset
	private LinkedList<Edge> optimalSlowRecursive(LinkedList<Edge> MST, LinkedList<Edge> currentCandidates, int[] unionFind, LinkedList<Edge> deletedEdges) {
		if(MST.size() == this.size-1) {
			return MST;
		}
		if(currentCandidates.size() <= 0) {
			System.out.println("Ran out of edges during recursion");
			return null;
		}
		
		LinkedList<Edge> deleted = new LinkedList<Edge>();
		deleted.addAll(deletedEdges);
		
		this.edgesVisitedOptimalSlow += 1;
		
		Edge nextEdge = currentCandidates.removeFirst();
		
		int v = getRepresentative(unionFind, nextEdge.getSrc());
		int w = getRepresentative(unionFind, nextEdge.getDest());
		
		if(v != w) {
			if(isCrossing(nextEdge, MST)) {
				LinkedList<Edge> ST1 = new LinkedList<Edge>();
				LinkedList<Edge> currentMST = new LinkedList<Edge>();
				currentMST.addAll(MST);
				ST1 = optimalSlowRecursive(MST, currentCandidates, unionFind, deleted);

				//remove crossing edges from alternative MST containing the edge
				LinkedList<Edge> ST2 = new LinkedList<Edge>();
				ST2 = nextEdge.nonCrossingEdges(currentMST);
				ST2.add(nextEdge);
				deleted.addAll(nextEdge.crossingEdges(currentMST));
				
				//recalculate unionFind for alternative MST
				int[] unionFind2 = new int[this.size];
				for (int i = 0; i < unionFind2.length; i++) {
					unionFind2[i] = -1;
				}

				for (Edge edge : ST2) {

					v = getRepresentative(unionFind2, edge.getSrc());
					w = getRepresentative(unionFind2, edge.getDest());
					if(v != w) {
						union(unionFind2, v, w);
					}
					else System.out.println("Something went horribly wrong");
				}
				
				//recalculate viable candidate edges for alternative MST
				LinkedList<Edge> candidates2 = new LinkedList<Edge>();
				for(Edge edge : this.edgeList) {
					if (!(ST2.contains(edge) || deleted.contains(edge))) {
						//should be sorted
						candidates2.add(edge);					
					}
				}
				
				//calculate alternative spanning tree
				ST2 = optimalSlowRecursive(ST2, candidates2, unionFind2, deleted);
				
				double weightST1 = 0;
				double weightST2 = 0;
				
				if(ST1 == null) {
					weightST1 = Double.MAX_VALUE;
				}
				else {
					for(Edge edge : ST1) {
						weightST1 += edge.getWeight();
					}
				}
				if(ST2 == null) {
					weightST2 = Double.MAX_VALUE;
				}
				else {
					for(Edge edge : ST2) {
						weightST2 += edge.getWeight();
					}
				}
				
				//choose more optimal spanning tree
				if(weightST1 <= weightST2) {
					MST = ST1;
				}
				else {
					MST = ST2;
				}
				return MST;
			}

			MST.add(nextEdge);
			union(unionFind, v,w);
			return optimalSlowRecursive(MST, currentCandidates, unionFind, deleted);
		}

		return optimalSlowRecursive(MST, currentCandidates, unionFind, deleted);
	}
	
	
	//Branch-and-bound algorithm to compute planar minimal spanning tree on the pointset
	public int branchAndBound() {
		this.completeGraphSetminusPK();
		
		/*
		// Uncomment to use swept area instead of initial length as weight
		for(Edge edge : edgeList) {
			edge.areaAsWeight();
		}
		*/
		
		Collections.sort(this.edgeList);
		LinkedList<Edge> currentCandidates = new LinkedList<Edge>();

		for(Edge edge : this.edgeList) {
			currentCandidates.add(edge);
		}

		this.edgesVisitedBranchAndBound = 0;
		
		//union-find
		int[] parents = new int[this.size];
		for (int i = 0; i < parents.length; i++) {
			parents[i] = -1;
		}
		
		LinkedList<Edge> MST = new LinkedList<Edge>();
		LinkedList<Edge> deletedEdges = new LinkedList<Edge>();
		MST = branchAndBoundRecursive(MST, currentCandidates, parents, deletedEdges, Double.MAX_VALUE);
		this.edgeList = MST;
		
		return this.edgesVisitedBranchAndBound;
	}
	
	
	private LinkedList<Edge> branchAndBoundRecursive(LinkedList<Edge> MST, LinkedList<Edge> currentCandidates, int[] unionFind, LinkedList<Edge> deletedEdges , double lowerBound) {
		//if ST is complete
		if(MST.size() == this.size-1) {
			return MST;
		}
		
		// if not enough edges left
		if(currentCandidates.size() < (this.size-1)-MST.size()) {
			//System.out.println("Ran out of edges during recursion");
			return null;
		}
		
		//prune recursions that can't beat the current optimum
		double bestCaseCost = 0;
		for (Edge edge : MST) {
			bestCaseCost += edge.getWeight();
		}
		
		for (int i = 0; i < (this.size-1)-MST.size(); i++) {
			bestCaseCost += currentCandidates.get(i).getWeight();
		}
		
		if(bestCaseCost >= lowerBound) {
			return null;
		}
		
		LinkedList<Edge> deleted = new LinkedList<Edge>();
		deleted.addAll(deletedEdges);
		
		this.edgesVisitedBranchAndBound += 1;
		
		Edge nextEdge = currentCandidates.removeFirst();
		
		int v = getRepresentative(unionFind, nextEdge.getSrc());
		int w = getRepresentative(unionFind, nextEdge.getDest());
		
		if(v != w) {
			if(isCrossing(nextEdge, MST)) {
				LinkedList<Edge> ST1 = new LinkedList<Edge>();
				LinkedList<Edge> currentMST = new LinkedList<Edge>();
				currentMST.addAll(MST);
				ST1 = branchAndBoundRecursive(MST, currentCandidates, unionFind, deleted, lowerBound);
				
				double weightST1 = 0;
				if(ST1 == null) {
					weightST1 = Double.MAX_VALUE;
				}
				else {					
					for(Edge edge : ST1) {
						weightST1 += edge.getWeight();
					}
				}
				
				lowerBound = Math.min(lowerBound, weightST1);
				
				//calculate alternative spanning tree

				//remove crossing edges from alternative MST containing the edge
				LinkedList<Edge> ST2 = new LinkedList<Edge>();
				ST2 = nextEdge.nonCrossingEdges(currentMST);
				ST2.add(nextEdge);
				deleted.addAll(nextEdge.crossingEdges(currentMST));
				
				//recalculate unionFind for alternative MST
				int[] unionFind2 = new int[this.size];
				for (int i = 0; i < unionFind2.length; i++) {
					unionFind2[i] = -1;
				}

				for (Edge edge : ST2) {
					v = getRepresentative(unionFind2, edge.getSrc());
					w = getRepresentative(unionFind2, edge.getDest());
					if(v != w) {
						union(unionFind2, v, w);
					}
					else System.out.println("Something went horribly wrong");
				}
				
				//recalculate viable candidate edges for alternative MST
				LinkedList<Edge> candidates2 = new LinkedList<Edge>();
				for(Edge edge : this.edgeList) {
					if (!(ST2.contains(edge) || deleted.contains(edge))) {
						//should be sorted
						candidates2.add(edge);					
					}
				}
				
				ST2 = branchAndBoundRecursive(ST2, candidates2, unionFind2, deleted, lowerBound);
				
				double weightST2 = 0;
				
				if(ST2 == null) {
					weightST2 = Double.MAX_VALUE;
				}
				else {
					for(Edge edge : ST2) {
						weightST2 += edge.getWeight();
					}
				}
				
				//choose more optimal spanning tree
				if(weightST1 <= weightST2) {
					MST = ST1;
				}
				else {
					MST = ST2;
				}
				return MST;
			}

			MST.add(nextEdge);
			union(unionFind, v,w);
			return branchAndBoundRecursive(MST, currentCandidates, unionFind, deleted, lowerBound);
		}

		return branchAndBoundRecursive(MST, currentCandidates, unionFind, deleted, lowerBound);
	}


	//Union-Find functionality
	private static int getRepresentative(int[] parents, int child) {
		if (parents[child] == -1) {
			return child;
		}
		return getRepresentative(parents, parents[child]);
	}
	
	private static void union(int[] parents, int src, int dest) {
		int rep1 = getRepresentative(parents, src);
		int rep2 = getRepresentative(parents, dest);
		parents[rep1] = rep2;
	}

	//computes a y-monotone path on the pointset
	public double yMonotonePath() {
		MovingPointSet2D points = new MovingPointSet2D(this.vertices);
		points.sortY();
		this.vertices = points;
		double weight = 0;
		this.edgeList = new LinkedList<Edge>();
		clearAdjList();
		LineSegment line;
		Edge nextEdge;
		
		for (int i = 0; i < this.size - 1; i++) {
			adjList[i].add(i+1);
			adjList[i+1].add(i);
			line = new LineSegment(vertices.getPoint(i), vertices.getPoint(i+1));
			nextEdge = new Edge(i, i+1, line);
			
			/*
			//uncomment to use swept area as weight
			nextEdge.areaAsWeight();
			*/
			
			edgeList.add(nextEdge);
			weight += nextEdge.getWeight();
		}
		
		return weight;
	}
	
	//returns true if argument edge crosses an edge in argument edgeList
	private boolean isCrossing(Edge edge, LinkedList<Edge> edgeList) {
		boolean isCrossing = edge.isCrossing(edgeList);
		this.comparisonsCrossingCriterion += edge.getComparisons();
		return isCrossing;
	}
	
	//compares the object to another instance of GraphDrawing and stores deviations
	public void findDifferences(GraphDrawing graph2) {
		LinkedList<Edge> missing = new LinkedList<Edge>();
		LinkedList<Edge> additional = new LinkedList<Edge>();
		LinkedList<Edge> altEdgeList = graph2.getEdgeList();
		boolean found = false;
		for (Edge edge : altEdgeList) {
			found = false;
			for(Edge edge2 : this.edgeList) {
				if(edge.isIdentical(edge2)) {
					found = true;
				}
			}
			if(!found) {
				missing.add(edge);
			}
		}
		for (Edge edge : this.edgeList) {	
			found = false;
			for(Edge edge2 : altEdgeList) {
				if(edge.isIdentical(edge2)) {
					found = true;
				}
			}
			if(!found) {
				additional.add(edge);
			}
		}
		
		this.missingEdges = missing;
		this.additionalEdges = additional;
	}
	
	//returns true if an edge is contained whose endpoints stay coincident with the arguments for the entire movement
	public boolean containsEdge(MovingPoint2D a, MovingPoint2D b) {
		MovingPoint2D start;
			MovingPoint2D end;
		
		for(Edge edge : this.edgeList) {
			start = edge.getPosition().getStartPoint();
			end = edge.getPosition().getEndPoint();
			if (start.coincides(a)) {
				if(end.coincides(b)) {
					return true;
				}
			}
			else if(start.coincides(b)) {
				if(end.coincides(a)) {
					return true;
				}
			}
		}
		return false;
	}

	
	private void clearAdjList() {
		for (int i = 0; i < size; i++) {
			adjList[i] = new LinkedList<Integer>();
		}
	}
	
	public double getTotalWeight() {
		double totalWeight = 0;
		for (Edge edge : this.edgeList) {
			totalWeight += edge.getWeight();
		}
		return totalWeight;
	}
	
	public int getEdgesVisitedGreedy() {
		return this.edgesVisitedGreedy;
	}
	
	public int getEdgesVisitedOptimalSlow() {
		return this.edgesVisitedOptimalSlow;
	}
	
	public int getEdgesVisitedBranchAndBound() {
		return this.edgesVisitedBranchAndBound;
	}
	
	public void sortEdges() {
		Collections.sort(this.edgeList);
	}
	
	public LinkedList<Edge>getEdgeList() {
		return this.edgeList;
	}
	
	public int getN() {
		return this.size;
	}
	
	public LinkedList<Edge> getMissingEdges(){
		return this.missingEdges;	
		}

	public int getComparisonsCrossingCriterion() {
		return this.comparisonsCrossingCriterion;
	}
	
}
