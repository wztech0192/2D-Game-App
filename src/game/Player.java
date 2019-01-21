package game;

import object.Point;

class Player extends Point{
	
	private boolean up=false;
	private boolean down=false;
	private boolean left=false;
	private boolean right=false;
	
	private Point leftHand;
	private Point rightHand;
	
	private int width,height;
	
	Player(double x, double y , double size) {
		super(x,y,size);
	}
	
	Player(double x, double y, int width, int height){
		super(x,y);
		this.width=width;
		this.height=height;
	}
	
	int getWidth() {
		return width;
	}
	
	int getHeight() {
		return height;
	}
	
	int getWidthRadius() {
		return width/2;
	}
	
	int getHeightRadius() {
		return height/2;
	}
	
	void createLeftHand() {
		leftHand=new Point();
		leftHand.setDiameter(d);
	}
	
	Point getLeftHand() {
		return leftHand;
	}
	
	void createRightHand() {
		rightHand=new Point();
		rightHand.setDiameter(d);
	}
	
	Point getRightHand() {
		return rightHand;
	}
	
	void updateHand() {
		leftHand.setDiameter(d);
		rightHand.setDiameter(d);
	}
	
	void setHandPosition(Point hand,double angle) {
		double newX = x + d * Math.cos(Math.toRadians(angle));
		double newY = y+ d * Math.sin(Math.toRadians(angle));
		hand.setLocation(newX, newY);
	}
	
	
	void setMoveUp(boolean in) {
		up=in;
	}
	void setMoveDown(boolean in) {
		down=in;
	}
	void setMoveLeft(boolean in) {
		left=in;
	}
	void setMoveRight(boolean in) {
		right=in;
	}
	void stopMove() {
		left=false;
		right=false;
		up=false;
		down=false;
	}
	
	boolean getMoveUp() {
		return up;
	}
	boolean getMoveDown() {
		return down;
	}
	boolean getMoveLeft() {
		return left;
	}
	boolean getMoveRight() {
		return right;
	}
	
	double findDist(Point t, Point b) {
		return Math.sqrt(Math.pow((b.getX()-t.getX()),2) + Math.pow((b.getY()-t.getY()),2));	
	}
	
	
}
