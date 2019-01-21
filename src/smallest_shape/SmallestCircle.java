/*
	**********************************
	File Name: SmallestCircle.java
	Package: object
	
	Credit Goes To: https://www.nayuki.io/page/smallest-enclosing-circle
	**********************************

	Purpose:
	*Find smallest enclosing circle using O(N) approach
*/


package smallest_shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.*;
import object.Point;

public final class SmallestCircle {
	

	private Circle circle;
	
	public void clearIfExist() {
		if(circle!=null) {
			circle.clear();
			circle = null;
		}
	}
	

	public Point[] getEdge() {
		return circle.getEdge();
	}
	
	public void makeCircle(ArrayList<Point> points) {
		circle = null;
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			if (circle == null || !circle.contains(p))
				circle = makeCircleOnePoint(points.subList(0, i + 1), p);
		}
	}
	
	public void drawCircle(Graphics2D g2d) {
		g2d.setColor(new Color(0f,1f,0f,.2f ));
		g2d.fill(new Ellipse2D.Double(circle.c.getX()-circle.r,circle.c.getY()-circle.r ,circle.r*2, circle.r*2));
		g2d.setColor(Color.green);
		g2d.drawOval((int)circle.c.getX()-2, (int)circle.c.getY()-2, 4,4);
	}
	
	
	private Circle makeCircleOnePoint(List<Point> points, Point p) {
		Circle c = new Circle(p, 0);
		for (int i = 0; i < points.size(); i++) {
			Point q = points.get(i);
			if (!c.contains(q)) {
				if (c.r == 0)
					c = makeDiameter(p, q);
				else
					c = makeCircleTwoPoints(points.subList(0, i + 1), p, q);
			}
		}
		return c;
	}
	
	private Circle makeCircleTwoPoints(List<Point> points, Point p, Point q) {
		Circle circ = makeDiameter(p, q);
		Circle left = null;
		Circle right = null;

		Point pq = q.subtract(p);
		for (Point r : points) {
			if (circ.contains(r))
				continue;
			
			double cross = pq.cross(r.subtract(p));
			Circle c = makeCircumcircle(p, q, r);
			if (c == null)
				continue;
			else if (cross > 0 && (left == null || pq.cross(c.c.subtract(p)) > pq.cross(left.c.subtract(p))))
				left = c;
			else if (cross < 0 && (right == null || pq.cross(c.c.subtract(p)) < pq.cross(right.c.subtract(p))))
				right = c;
		}
		
		if (left == null && right == null)
			return circ;
		else if (left == null)
			return right;
		else if (right == null)
			return left;
		else
			return left.r <= right.r ? left : right;
	}
	
	
	private Circle makeDiameter(Point a, Point b) {
		Point c = new Point((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
		Circle d=new Circle(c, Math.max(c.distance(a), c.distance(b)));
		d.setEdge(a,b,null);
		return d;
	}
	
	
	private Circle makeCircumcircle(Point a, Point b, Point c) {
		double ox = (Math.min(Math.min(a.getX(), b.getX()), c.getX()) + Math.max(Math.min(a.getX(), b.getX()), c.getX())) / 2;
		double oy = (Math.min(Math.min(a.getY(), b.getY()), c.getY()) + Math.max(Math.min(a.getY(), b.getY()), c.getY())) / 2;
		double ax = a.getX() - ox, ay = a.getY() - oy;
		double bx = b.getX() - ox, by = b.getY() - oy;
		double cx = c.getX() - ox, cy = c.getY() - oy;
		double d = (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) * 2;
		if (d == 0)
			return null;
		double x = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d;
		double y = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d;
		Point p = new Point(ox + x, oy + y);
		double r = Math.max(Math.max(p.distance(a), p.distance(b)), p.distance(c));
		
		Circle cir= new Circle(p, r);
		cir.setEdge(a, b,c);
		return cir;
	}
	
	private class Circle{
		private Point[] edge=new Point[3];
		private static final double MULTIPLICATIVE_EPSILON = 1 + 1e-14;
		
		private final Point c;   // Center
		private final double r;  // Radius
		
		
		
		private Circle(Point c, double r) {
			this.c = c;
			this.r = r;
		}
		
		private void clear() {
			edge=null;
		}

		private boolean contains(Point p) {
			return c.distance(p) <= r * MULTIPLICATIVE_EPSILON;
		}
		private void setEdge(Point a, Point b, Point c) {
			edge[0]=a;
			edge[1]=b;
			edge[2]=c;
		}
		
		private Point[] getEdge() {
			return edge;
		}
	}
}