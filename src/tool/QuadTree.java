/*
	**********************************
	File Name: QuadTree.java
	Package: object
	
	Credit Goes To: https://gamedevelopment.tutsplus.com/tutorials/quick-tip-use-quadtrees-to-detect-likely-collisions-in-2d-space--gamedev-374
	**********************************

	Purpose:
	*QuadTree Data Structure: recursively cutting map into four quadrant and fits points inside the belonging quadrant.
*/

package tool;

import java.util.LinkedList;
import java.util.List;

import object.Point;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

public class QuadTree {
 
  private final static int MAX_LEVELS = 6;
  private final static int MAX_OBJECT=15;
  
  private int level;
  private List<Point> objects;
  private Rectangle bounds;
  private QuadTree[] nodes;
  
  
  
  public QuadTree(int pLevel, Rectangle pBounds) {
	  level = pLevel;
	  objects = new LinkedList<Point>();
	  bounds = pBounds;
	  nodes = new QuadTree[4];
  }
  
  public void setBounds(int w, int h) {
	  bounds.setBounds(0, 0, w, h);
	  
  }
  
  public void clear() {
	  objects.clear();
	  for (int i = 0; i < nodes.length; i++) {
	     if (nodes[i] != null) {
	    	nodes[i].clear();
	       	nodes[i] = null;
	     }
	  }
  }
  
  private void split() {
	   int subWidth = (int)(bounds.getWidth() / 2);
	   int subHeight = (int)(bounds.getHeight() / 2);
	   int x = (int)bounds.getX();
	   int y = (int)bounds.getY();
	 
	   nodes[0] = new QuadTree(level+1, new Rectangle(x + subWidth, y, subWidth, subHeight));
	   nodes[1] = new QuadTree(level+1, new Rectangle(x, y, subWidth, subHeight));
	   nodes[2] = new QuadTree(level+1, new Rectangle(x, y + subHeight, subWidth, subHeight));
	   nodes[3] = new QuadTree(level+1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
  }
  
  private int getIndex(Point p) {
	   int index = -1;
	   double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
	   double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);
	   double top=p.getY()-p.getRadius();
	   double bot=p.getY()+p.getRadius();
	   double left=p.getX()-p.getRadius();
	   double right=p.getX()+p.getRadius();
	   // Object can completely fit within the top quadrants
	   boolean topQuadrant = (top < horizontalMidpoint && bot < horizontalMidpoint);
	   // Object can completely fit within the bottom quadrants
	   boolean bottomQuadrant = (top> horizontalMidpoint);
	 
	   // Object can completely fit within the left quadrants
	   if (left < verticalMidpoint && right < verticalMidpoint) {
		   if (topQuadrant) {
			   index = 1;
		   }
		   else if (bottomQuadrant) {
			   index = 2;
		   }
	   }
	   // Object can completely fit within the right quadrants
	   else if (left > verticalMidpoint) {
		   if (topQuadrant) {
			   index = 0;
		   }
		   else if (bottomQuadrant) {
			   index = 3;
		   }
	   }
	   return index;
  }
  
  public void insert(Point point) {
	  if (nodes[0] != null) {
		  int index = getIndex(point);
		  if (index != -1) {
		      nodes[index].insert(point);
		      return;
		  }
	  }
	  objects.add(point);
	  if(objects.size()>MAX_OBJECT && level < MAX_LEVELS) {
		  if (nodes[0] == null) { 
			  split(); 
		  }
		  int i = 0;
		  while (i < objects.size()) {
			  int index = getIndex((Point)objects.get(i));
			  if(index != -1) {
				  nodes[index].insert((Point)objects.remove(i));
			  }
			  else {
				  i++;
			  }
		  }
	  }
  }
  
  public void retrieve(LinkedList<Point> returnObjects, Point point) {
	  if(nodes[0]!=null) {
		   int index = getIndex(point);
		  
		   if (index != -1) {
			   nodes[index].retrieve(returnObjects, point);
		   }else {
			  /*for(QuadTree n:nodes) {
				   n.retrieve(returnObjects, point);
			   }*/
		   }
	  }
	  returnObjects.addAll(objects);
	  return ;
  }
  
  public void retrieveAndDelete(LinkedList<Point> returnObjects, Point point) {
	   int index = getIndex(point);
	   if (index != -1 && nodes[0] != null) {
	     nodes[index].retrieve(returnObjects, point);
	   }
	   
	   objects.remove(point);
	   returnObjects.addAll(objects);
	   return ;
  }
  
  public void remove(Point point) {
	   int index = getIndex(point);
	   if (index != -1 && nodes[0] != null) {
	     nodes[index].remove(point);
	   }
	   objects.remove(point);
	   return;
  }
  
  public void getGrid(Graphics2D g2d){
	   if (nodes[0] != null) {
	     nodes[0].getGrid(g2d);
	   }
	   if (nodes[1] != null) {
		     nodes[1].getGrid(g2d);
	   }
	   if (nodes[2] != null) {
		     nodes[2].getGrid(g2d);
		   }
	   if (nodes[3] != null) {
		     nodes[3].getGrid(g2d);
		}
	   g2d.draw(new Rectangle2D.Double(bounds.x, bounds.y, bounds.getWidth(),bounds.getHeight()));
	   return;
  	}
}