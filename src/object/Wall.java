/*
	**********************************
	File Name: Wall.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Create a line which block and deflect all the Point pass by
*/

package object;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import object.Point;

public class Wall {
	private ArrayList<Point> drawPoint=new ArrayList<Point>(10);
	private int num=0;
	
	
	public void addPoint(Point p) {
		drawPoint.add(p);
		num++;
	}
	
	public void clearPoint() {
		if(num>0) {
			drawPoint.clear();
			num=0;
		}
	}
	
	public boolean isEmpty() {
		return num==0;
	}
	
	public void checkLine(Point p) {
		for(int i=0;i<num;++i) {
			if(i%2!=0) {
				if(Line2D.ptSegDist(drawPoint.get(i).getX(),drawPoint.get(i).getY(),drawPoint.get(i-1).getX(),drawPoint.get(i-1).getY(), p.getX(), p.getY())<p.getRadius()+5) {
					p.setXS(-p.getXS());
					p.setYS(-p.getYS());
				}
			}
		}
	}

	public void drawWall(Graphics2D g2d) {
		for(int i=0;i<num;i++) {			
			g2d.draw(new Rectangle2D.Double(drawPoint.get(i).getX()-2, drawPoint.get(i).getY()-2, 4, 4));
			if(i%2!=0)
				g2d.draw(new Line2D.Double(drawPoint.get(i).getX(), drawPoint.get(i).getY(),drawPoint.get(i-1).getX(), drawPoint.get(i-1).getY()));
		}
	}
	
	public Point getLast() {
		return drawPoint.get(num-1);
	}

	
}
