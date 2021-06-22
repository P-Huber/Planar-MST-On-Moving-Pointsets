package drawings;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

public class MovingPoint2D implements Comparable{
	
	private int xCoord;
	private int yCoord;
	private int xMovement;
	private int yMovement;
	
	public MovingPoint2D() {};
	
	public MovingPoint2D(int x, int y, int dx, int dy) {
		this.xCoord = x;
		this.yCoord = y;
		this.xMovement = dx;
		this.yMovement = dy;
	}
	
	public MovingPoint2D(MovingPoint2D another) {
		this.xCoord = another.getxCoord();
		this.yCoord = another.getyCoord();
		this.xMovement = another.getxMovement();
		this.yMovement = another.getyMovement();
	}

	
	//randomizes the coordinate and movement fields of this, according to the specified bounds
	public void randomizePoint(int xLeft, int xRight, int yLower, int yUpper, int dxLower, int dxUpper, int dyLower, int dyUpper, Random random) {
		this.xCoord = (int) (xLeft + random.nextDouble() * (xRight - xLeft));
		this.yCoord = (int) (yLower + random.nextDouble() * (yUpper - yLower));
		this.xMovement = (int) (dxLower + random.nextDouble() * (dxUpper - dxLower));
		this.yMovement = (int) (dyLower + random.nextDouble() * (dyUpper - dyLower));
	}
	
	

	// returns the angle of pp1 to pp2 in clockwise direction(or as negative angle in counterclockwise direction)
	// (p,p1,p2) -> [-180, 180]
	public double angle(MovingPoint2D p1, MovingPoint2D p2) {
		double dxa = this.xCoord - p1.getxCoord();
		double dya = this.yCoord - p1.getyCoord();
		double dxb = this.xCoord - p2.getxCoord();
		double dyb = this.yCoord - p2.getyCoord();
		double aDotb = (dxa*dxb) + (dya*dyb);
		double det = (dxa*dyb - dya*dxb);
		double angle = Math.toDegrees(Math.atan2(det, aDotb));
		return angle;
	}
	
	// returns the angle of pp1 to pp2 in clockwise direction(or as negative angle in counterclockwise direction) after the movement
		// (p,p1,p2) -> [-180, 180]
	public double angleAfterMoving(MovingPoint2D p1, MovingPoint2D p2) {
		double dxa = (this.xCoord + this.xMovement) - (p1.getxCoord() + p1.getxMovement());
		double dya = (this.yCoord + this.yMovement) - (p1.getyCoord() + p1.getyMovement());
		double dxb = (this.xCoord + this.xMovement) - (p2.getxCoord() + p2.getxMovement());
		double dyb = (this.yCoord + this.yMovement) - (p2.getyCoord() + p2.getyMovement());
		double aDotb = (dxa*dxb) + (dya*dyb);
		double det = (dxa*dyb - dya*dxb);
		double angle = Math.toDegrees(Math.atan2(det, aDotb));
		return angle;
	}
	
	//returns the euclidean distance between two non-moving points given their position
	public static double pointDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	//returns true if this coincides with the argument MovingPoint2D
	public boolean coincides(MovingPoint2D p2) {
		if (this.xCoord == p2.getxCoord() && this.yCoord == p2.getyCoord() && this.xMovement == p2.getxMovement() && this.yMovement == p2.getyMovement()){
			return true;
		}
		return false;
	}
	
	//returns the angle between three points given their coordinates
	public static double angleBetween(int x1, int y1, int x2, int y2, int x3, int y3) {

		  return Math.toDegrees(Math.atan2(x2 - x1, y2 - y1) -
		                        Math.atan2(x3 - x1, y3 - y1));
		}
	
	
	public int getxCoord() {
		return xCoord;
	}

	public void setxCoord(int xCoord) {
		this.xCoord = xCoord;
	}

	public int getyCoord() {
		return yCoord;
	}

	public void setyCoord(int yCoord) {
		this.yCoord = yCoord;
	}

	
	public int getxMovement() {
		return xMovement;
	}

	public void setxMovement(int xMovement) {
		this.xMovement = xMovement;
	}

	public int getyMovement() {
		return yMovement;
	}

	public void setyMovement(int yMovement) {
		this.yMovement = yMovement;
	}

	public String pointToString() {
		return ("(" + this.xCoord + ", " + this.yCoord +")");
	}
	
	public boolean startsEqualTo(MovingPoint2D p2) {
		return (this.xCoord == p2.getxCoord() && this.yCoord == p2.getyCoord());
	}
	
	
    @Override
    public int compareTo(Object o) {
    	if (this.yCoord == (((MovingPoint2D) o).getyCoord())) {
    		return Double.compare(this.xCoord, (((MovingPoint2D) o).getxCoord()));
    	}
    	else {
    		return Double.compare(this.yCoord, (((MovingPoint2D) o).getyCoord()));
    	}

    }
	
}
