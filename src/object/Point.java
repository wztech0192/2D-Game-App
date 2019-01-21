/*
	**********************************
	File Name: Point.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*store velocity
	*store coordinate
	*display the shape
*/

package object;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import tool.Animator;


public class Point{
	
	protected double x;
	protected double y;
	protected double r;
	protected double d;
	protected double xS;
	protected double yS;
	protected int defLifeSpan;
	protected int lifeSpan;
	protected BufferedImage image;
	protected double angle;
	private Color color;
	private int useValue;
	private boolean moveDirection;
	private int moveAngle;
	private Animator animator;
	
	public Point() {
	}
	
	
	public Point(double x, double y) {
		this.x=x;
		this.y=y;
	}
	
	public Point(double x, double y, double d) {
		this(x,y);
		this.d=d;
		this.r=d/2;
	}
	
	public Point(double x, double y, double d, double xS, double yS) {
		this(x,y,d);
		this.xS=xS;
		this.yS=yS;
	}
	
	public Point(double x, double y, double d, double xS, double yS,int defLifeSpan) {
		this(x,y,d,xS,yS);
		this.setDefaultLifeSpanAndLifeSpan(defLifeSpan);
	}
	
	public void setMoveDirection(boolean moveDirection) {
		this.moveDirection=moveDirection;
	}
	
	public boolean getMoveDirection() {
		return moveDirection;
	}
	
	public void setMoveAngle(int moveAngle) {
		this.moveAngle=moveAngle;
	}
	
	public int getMoveAngle() {
		return moveAngle;
	}
	
	public void setUseValue(int useValue) {
		this.useValue=useValue;
	}
	
	public int getUseValue() {
		return useValue;
	}
	
	public void reduceUseValue() {
		useValue--;
	}
	
	public void addUseValue() {
		useValue++;
	}
	
	public void setColor(Color color) {
		this.color=color;
	}
	
	public Color getColor() {
		return color;
	}
	
	public int getLifeSpan() {
		return lifeSpan;
	}
	public void addLifeSpan() {
		lifeSpan++;
	}
	public void reduceLifeSpan() {
		lifeSpan--;
	}
	public void setLifeSpan(int i) {
		lifeSpan=i;
	}
	public void resetLifeSpan() {
		lifeSpan=defLifeSpan;
	}
	public int getDefaultLifeSpan() {
		return defLifeSpan;
	}
	public void setDefaultLifeSpan(int initLifeSpan) {
		this.defLifeSpan=initLifeSpan;
	}
	public void setDefaultLifeSpanAndLifeSpan(int initLifeSpan) {
		this.defLifeSpan=initLifeSpan;
		lifeSpan=initLifeSpan;
	}
	public Point subtract(Point p) {
		return new Point(x - p.x, y - p.y,r,xS,yS);
	}
	
	
	public double distance(Point p) {
		return Math.hypot(x - p.x, y - p.y);
	}
	
	public double cross(Point p) {
		return x * p.y - y * p.x;
	}
	
	public void setLocation(double x, double y) {
		this.x=x;
		this.y=y;
	}
	
	public void setAnimator(Animator animator) {
		this.animator=animator;
		image=animator.getDefaultImage();
	}
	
	public Animator getAnimator() {
		return animator;
	}
	
	public void setImage(BufferedImage image) {
		this.image=image;
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setAngle(Point p) {
		angle =(Math.atan2(y-p.getY(),x-p.getX()));
	}
	public void setAngle(Point p1, Point p2) {
		angle =(Math.atan2(p1.getY()-p2.getY(),p1.getX()-p2.getX()));
	}
	
	public void setAngle(double angle) {
		this.angle=angle;
	}
	
	public double getAngle() {
		double angleInDegree=Math.toDegrees(angle);
		if(angleInDegree>0) {
			return angleInDegree;
		}else {
			return angleInDegree+360;
		}

	}
	

	public boolean hasImage() {
		return image!=null;
	}
	

	public void drawShape(Graphics2D g2d) {
		g2d.fill(new Ellipse2D.Double(x-r,y-r,d,d));
	}	

	public void drawWithImageBigger(Graphics2D g2d) {
		if(image!=null){
			if(animator!=null)
				image=animator.getImage();
			AffineTransform oldAT = g2d.getTransform();
            g2d.rotate(angle,x,y);
            g2d.drawImage(image,(int)(x-d),(int)(y-d),(int)(d*2),(int)(d*2),null);
            g2d.setTransform(oldAT);
		}
		else {
			g2d.fill(new Ellipse2D.Double(x-r,y-r,d,d));
		}
	}
	
	
	public void drawWithImage(Graphics2D g2d) {
		if(image==null) {
			g2d.fill(new Ellipse2D.Double(x-r,y-r,d,d));
		}else {
			AffineTransform oldAT = g2d.getTransform();
            g2d.rotate(angle,x,y);
            g2d.drawImage(image,(int)(x-r),(int)(y-r),(int)(d),(int)(d),null);
            g2d.setTransform(oldAT);
		}
	}
	
	public void drawWithColor(Graphics2D g2d) {
		g2d.setColor(color);
		g2d.fill(new Ellipse2D.Double(x-r,y-r,d,d));
	}
	 
	public void move(int width, int height) {
	    if (x-r < 0) {
	    	x=r;
	    	if(xS<0)
	    		xS*=(xS>2)?-0.5:-1;
	    } else if (x+r>width) {
	    	x=width-r;
	    	if(xS>0)
	    		xS*=(xS>2)?-0.5:-1;
		}
        if (y-r<0) {
        	y=r;
        	if(yS<0)
        		yS*=(yS>2)?-0.5:-1;
        }
	    else if (y+r>height) {
	    	y=height-r;
	    	if(yS>0)
	    		yS*=(yS>2)?-0.5:-1;
	    }
        move();
	}
	
	public void move() {
	//	System.out.println(x+" "+xS);
		x+=xS;
		y+=yS;
	}
	   
	public double getXS() {
		return xS;
	}
	public void setXS(double xS) {
	    this.xS = xS;
    }
    public double getYS() {
        return yS;
    }
    public void setYS(double yS) {
        this.yS = yS;
    }
    public void setX(double x) {
    	this.x=x;
    }
    public double getX() {
    	return x;
    }
    
    public void setY(double y) {
    	this.y=y;
    }
    public double getY() {
    	return y;
    }
    public double getRadius() {
    	return r;
    }
    public void setDiameter(double d) {
    	this.d=d;
    	r=d/2;
    }
    public double getDiameter() {
    	return d;
    }
}