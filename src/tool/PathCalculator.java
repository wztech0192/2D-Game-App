/*
	**********************************
	File Name: CollisionDetector.java
	Package: PathFinder
	
	Author: Wei Zheng
	**********************************

	Purpose:
	* Calculate the path of point
	* Negative speed will result in opposite direction
*/

package tool;

import object.Point;

public class PathCalculator {
	
	//return velocity of two point
	public static Point getVelocity(Point p1, Point p2,double speed) {    
	    double xDif = p1.getX() - p2.getX(),
	            yDif = p1.getY() - p2.getY(),
	            angle = Math.atan2(xDif, yDif) / Math.PI,
	            xS = -Math.sin(angle * Math.PI) * speed,
	            yS = -Math.cos(angle * Math.PI) * speed;
	    return new Point(xS,yS);
	}

	public static Point getOrbitalPath(Point p1, Point p2,double speed) {
	    double xDif = p1.getX() - p2.getX(),
	            yDif = p1.getY() - p2.getY(),
	            angle = Math.atan2(xDif, yDif) / Math.PI,
	            xS = -Math.cos(angle * Math.PI) * speed,
	            yS = Math.sin(angle * Math.PI) * speed;
	    return new Point(xS,yS);
	}
	
	//find straight path of one point toward another point
	public static void straightPath(Point p1, Point p2, double speed) {
		double xDif=p1.getX()-p2.getX();
		double yDif=p1.getY()-p2.getY();
		double angle=Math.atan2(xDif, yDif)/Math.PI;
		p1.setXS(-Math.sin(angle*Math.PI)*speed);
	    p1.setYS(-Math.cos(angle*Math.PI)*speed);		
	}
	
	// find orbit path of one point to another point
	public static void orbitPath(Point p1, Point p2, double speed) {
		double xDif=p1.getX()-p2.getX();
		double yDif=p1.getY()-p2.getY();
		double angle = Math.atan2(xDif, yDif)/Math.PI;
	    p1.setXS(-Math.cos(angle*Math.PI)*speed);
	    p1.setYS(Math.sin(angle*Math.PI)*speed);
	}
	
	//find circular path of one point toward another point
	public static void circularPath(Point p1, Point p2, double speed, double angleAjdust) {
		Point path=adjustPathPoint(p1,p2,angleAjdust);
		orbitPath(p1,path,speed);
	}
	
	//adjust angle of a point
	public static Point adjustPathPoint(Point p1, Point p2, double angleAdjust) {
	    double angle = findAngle(p1,p2);
		double distance=findDist(p1,p2);
		double angleRad = (angle-angleAdjust)*Math.PI/180; 
		return findAdjustAnglePoint(p2,distance,-angleRad);
	}
	
	public static Point findAdjustAnglePoint(Point p, double distance, double angleRad) {
		double newX = p.getX() + distance * Math.cos(-angleRad);
		double newY = p.getY() + distance * Math.sin(-angleRad);
		return new Point(newX,newY);
	}
	
	public static int adjustAngle(int angle) {
		if(angle<0)
			angle+=360;
		else if(angle>360)
			angle-=360;
		return angle;
	}
	
	public static double findDist(Point b, Point b1) {
		return Math.sqrt(Math.pow((b.getX()-b1.getX()),2) + Math.pow((b.getY()-b1.getY()),2));	
	}	
	
	public static double findAngle(Point p1, Point p2) {
	    double angle =Math.toDegrees(Math.atan2(p1.getY() - p2.getY(), p1.getX()- p2.getX()));
		if(angle < 0){
		    angle += 360;
		}
		return angle;
	}
	
}
