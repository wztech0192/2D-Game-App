/*
	**********************************
	File Name: SmallestRectangle.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Find smallest enclosing rectangle 
*/

package smallest_shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import object.Point;


public class SmallestRect {
	private Point[] rectEdge=new Point[4];
	private Point center;	
	
	
	public void clearIfExist() {
		if(center!=null) {
			center=null;
			Arrays.fill(rectEdge, null);
		}
	}
	
	public Point[] getEdge() {
		return rectEdge;
	}
	
	public void drawRect(Graphics2D g2d) {
		g2d.setColor(new Color(0.3f,0.3f,0.8f,.2f ));
	
		g2d.fill(new Rectangle2D.Double(rectEdge[0].getX(), rectEdge[2].getY(), (rectEdge[1].getX()-rectEdge[0].getX()), (rectEdge[3].getY()-rectEdge[2].getY())));
		g2d.setColor(Color.blue);
		center.drawShape(g2d);
	}

	
	public void findCenter() {
		double x= (rectEdge[0].getX()+rectEdge[1].getX())/2;
		double y= (rectEdge[2].getY()+rectEdge[3].getY())/2;
		center=new Point(x,y);
		center.setDiameter(4);
	}
	
	public void checkEdge(ArrayList<Point> points) {

		Arrays.fill(rectEdge, points.get(0));
		points.forEach(p->{
			if(p.getX()<rectEdge[0].getX()) { //left most edge
				rectEdge[0]=p;
			}
			if(p.getX()>rectEdge[1].getX()) { //right most edge
				rectEdge[1]=p;
			}
			if(p.getY()<rectEdge[2].getY()) { //top most edge
				rectEdge[2]=p;
			}
			if(p.getY()>rectEdge[3].getY()) { //bottom most edge
				rectEdge[3]=p;
			}
		});
	}
}
