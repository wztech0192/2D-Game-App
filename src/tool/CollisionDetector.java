/*
	**********************************
	File Name: CollisionDetector.java
	Package: tool
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*dectect collision
	*Has two detecting method:
	*literate method- compare each Point to each other Point to see if collide. O(n^2)
	*quadtree method- use quadtree data structure to compare each Point to each Point inside its quadrant. O(nLogn)
	*
*/



package tool;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import object.Point;


public class CollisionDetector {

	private String collide_method;
	private QuadTree quadtree;

	public CollisionDetector() {
		this.collide_method="Collision??";
	}
	
	public void setCollideMethod(String collide_method) {
		this.collide_method=collide_method;
	}
		
	public void drawTreeGrid(Graphics2D g2d) {
		if(quadtree!=null) {
			quadtree.getGrid(g2d);
		}
	}
	
	public void clear() {
		if(quadtree!=null) {
			quadtree.clear();
			quadtree=null;
		}
	}
	
	public boolean testCollide(Point t,List<Point> points) {
		for(Point p:points) {
			if(checkCollide(t,p)) {
				return true;
			}
		}
		return false;
	}
	
	public void iterativeDetecting(ArrayList<Point> points) {
		if(quadtree!=null) {
			quadtree.clear();
			quadtree=null;
		}
		for(int a=0; a<points.size();++a) {
			Point p_a=points.get(a);
			for(int b=a+1; b<points.size();++b) {
				Point p_b=points.get(b);
				if(checkCollide(p_a,p_b)) {
					switch(collide_method) {
					case "Collision??":
						collide(p_a,p_b);
						break;
					case "Advanced Collision":
						collide1(p_a,p_b);
						break;
					case "Ghost Collision":
						collide2(p_a,p_b);
						break;
					case "Rotated Attraction":
						collide3(p_a,p_b);
						break;
					}
				}
			}
		}	
	}
	
	public void quadtreeDetecting(ArrayList<Point> points,int w, int h) {
		quadTreeInsert(points,w,h);
		LinkedList<Point> list=new LinkedList<Point>();
		for(Point p_a:points) {	
			list.clear();
			quadtree.retrieve(list, p_a);
			for(Point p_b:list) {
				if(p_a!=p_b) {
					if(checkCollide(p_a,p_b)) {
						switch(collide_method) {
						case "Collision??":
							collide(p_a,p_b);
							break;
						case "Advanced Collision":
							collide1(p_a,p_b);
							break;
						case "Ghost Collision":
							collide2(p_a,p_b);
							break;
						case "Rotated Attraction":
							collide3(p_a,p_b);
							break;
						}
					}
				}
			}
		}
	}
	
	
	public void quadTreeInsert(ArrayList<Point> points, int w, int h) {
		if(quadtree!=null) {
			quadtree.clear();
			quadtree.setBounds(w, h);
		}else {
			quadtree=new QuadTree(0,new Rectangle(0,0,w,h));
		}
		for(Point p_a:points) {
			quadtree.insert(p_a);
		}
	}
	
	public Point staticQuadTreeDetect(Point p_a) {
		
		LinkedList<Point> list=new LinkedList<Point>();
		quadtree.retrieve(list, p_a);
		for(Point p_b:list) {
			if(checkCollide(p_a,p_b)) {
				collide2(p_a,p_b);
				quadtree.remove(p_b);
				return p_b;
			}
		}
		return null;
	}
	
	
	private class Velocity{
		double x;
		double y;
		private Velocity(double xS,double yS) {
			this.x=xS;
			this.y=yS;
		}

	}
	
	private Velocity rotate(Velocity velocity, double angle) {
			double x= velocity.x * Math.cos(angle) - velocity.y * Math.sin(angle);
			double y= velocity.x * Math.sin(angle) + velocity.y * Math.cos(angle);
			velocity.x=x;
			velocity.y=y;
			return velocity;
	}
	
	public void collide(Point a, Point b) {
		double total=a.getDiameter()+b.getDiameter();
	    double newX1 =(a.getXS()*(a.getDiameter()-b.getDiameter())+(2*b.getDiameter()*b.getXS()))/total;
        double newY1 = (a.getYS()*(a.getDiameter()-b.getDiameter())+(2*b.getDiameter()*b.getYS()))/total;
        double newX2=(b.getXS()*(b.getDiameter()-a.getDiameter())+(2*a.getDiameter()*a.getXS()))/total;
        double newY2= (b.getYS()*(b.getDiameter()-a.getDiameter()) + (2*a.getDiameter()*a.getYS()))/total;	        
        b.setXS(newX2);
        b.setYS(newY2);
        a.setXS(newX1);
        a.setYS(newY1);    
        b.move();
        a.move();
	}
	
	public void collide1(Point a, Point b) {
	    double xVelocityDiff = a.getXS() - b.getXS();
	    double yVelocityDiff = a.getYS() - b.getYS();
	    double xDist = a.getX() - b.getX();
	    double yDist = a.getY() - b.getY();
	    
	    if (xVelocityDiff * xDist + yVelocityDiff * yDist <= 0) {
	        double angle = -Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
	        double m1 = a.getDiameter();
	        double m2 = b.getDiameter();
	        Velocity u1 = rotate(new Velocity(a.getXS(),a.getYS()), angle);
	        Velocity u2 = rotate(new Velocity(b.getXS(),b.getYS()), angle);
	        Velocity v1 = new Velocity(u1.x * (m1 - m2) / (m1 + m2) + u2.x * 2 * m2 / (m1 + m2), u1.y );		        	
	        Velocity v2 =new Velocity(u2.x * (m1 - m2) / (m1 + m2) + u1.x * 2 * m2 / (m1 + m2), u2.y );
	        Velocity vFinal1 = rotate(v1, -angle);
	        Velocity vFinal2 = rotate(v2, -angle);
	        a.setXS(vFinal1.x);
	        a.setYS(vFinal1.y);
	        b.setXS(vFinal2.x);
	        b.setYS(vFinal2.y);
	    }	    
	}
	
	public void collide2(Point a, Point b){
	   	double xVelocityDiff = a.getXS() - b.getXS();
	    double yVelocityDiff = a.getYS() - b.getYS();
	    double xDist = a.getX() - b.getX();
	    double yDist = a.getY() - b.getY();
	    if (xVelocityDiff * xDist + yVelocityDiff * yDist >= 0) {
			double total=a.getDiameter()+b.getDiameter();
		    double newX1 =(a.getXS()*(a.getDiameter()-b.getDiameter())+(2*b.getDiameter()*b.getXS()))/total;
	        double newY1 = (a.getYS()*(a.getDiameter()-b.getDiameter())+(2*b.getDiameter()*b.getYS()))/total;
	        double newX2=(b.getXS()*(b.getDiameter()-a.getDiameter())+(2*a.getDiameter()*a.getXS()))/total;
	        double newY2= (b.getYS()*(b.getDiameter()-a.getDiameter()) + (2*a.getDiameter()*a.getYS()))/total;	        
	        b.setXS(newX2);
	        b.setYS(newY2);
	        a.setXS(newX1);
	        a.setYS(newY1);
	        b.move();	    
	    }
	}
	
	public void collide3(Point a, Point b) {
	    double xVelocityDiff = a.getXS() - b.getXS();
	    double yVelocityDiff = a.getYS() - b.getYS();
	    double xDist = a.getX() - b.getX();
	    double yDist = a.getY() - b.getY();
	    
	    if (xVelocityDiff * xDist + yVelocityDiff * yDist >= (a.getRadius()+b.getRadius())) {
	        double angle = -Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
	        double m1 = a.getDiameter();
	        double m2 = b.getDiameter();
	        Velocity u1 = rotate(new Velocity(a.getXS(),a.getYS()), angle);
	        Velocity u2 = rotate(new Velocity(b.getXS(),b.getYS()), angle);
	        Velocity v1 = new Velocity(u1.x * (m1 - m2) / (m1 + m2) + u2.x * 2 * m2 / (m1 + m2), u1.y );		        	
	        Velocity v2 =new Velocity(u2.x * (m1 - m2) / (m1 + m2) + u1.x * 2 * m2 / (m1 + m2), u2.y );
	        Velocity vFinal1 = rotate(v1, -angle);
	        Velocity vFinal2 = rotate(v2, -angle);
	        a.setXS(vFinal1.x);
	        a.setYS(vFinal1.y);
	        b.setXS(vFinal2.x);
	        b.setYS(vFinal2.y);
	       
	    }	    
	}
	
	public boolean	checkCollide(Point a, Point b) {
		return (Math.sqrt(Math.pow((b.getX()-a.getX()),2) + Math.pow((b.getY()-a.getY()),2)))<=(a.getRadius()+b.getRadius());	
	}
	
	public boolean checkCollide(Point a, Point b, double dist) {
		return dist<=(a.getRadius()+b.getRadius());
	}

}
