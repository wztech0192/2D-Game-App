/*
	**********************************
	File Name: FratalTree.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Draw fractal tree use points
*/

package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JOptionPane;
import object.Board;
import object.Point;
import tool.ImageEditor;
import tool.PathCalculator;



class FractalTree extends GameSetter {
	private int index;
	private int num;
	
	private int startingSize;
	private int startingAngle;
	
	private int randBranch;
	
	private int branchAngle;
	private int branchLength;
	private int maxBranchLevel;
	private double branchSizeAdjust;
	private double branchLengthAdjust;

	private boolean randCurveBranch;
	private boolean randBranchLength;
	private boolean allInOnce;
	private int appearLocation;
	
	private int addPointNumber;
	private int pointSpeed;
	private int pauseNum;
	
	private int endColorIndex;
	private final Color[] c= {Color.white,Color.BLACK,Color.green,Color.red,Color.blue,Color.cyan,Color.orange};
	private final Point mouse=new Point(0,0);
	
	private ArrayList<Point> coordinate;
	private Dimension dim;
	private boolean clicked;
	private BufferedImage background;
	private Thread t;
	private BranchCalculator branchCalculator; 
	
	private boolean useImage;
	private BufferedImage leaf;
	private BufferedImage wood;
	
	public FractalTree(Board board) {
		super(board);
	}
	
	@Override
	public void gameClear() {
		if(t.isAlive()) {
			branchCalculator.stopRunning();	
		}
		if(!coordinate.isEmpty())
			coordinate.clear();
	}
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
    	boolean yy=false;
    	boolean xx=false;
    	
		switch(appearLocation) {
		case 1:
			yy=true;
			break;
		case 2:
			break;
		case 3:
			yy=rand.nextBoolean();
			xx=rand.nextBoolean();
		}

    	
    	if(xx && yy) {
    		return new Point(width,rand.nextInt(height));
    	}
    	else if(!xx && !yy) {
    		return new Point(rand.nextInt(width),height);
    	}
    	else if(yy) {
    		return new Point(rand.nextInt(width),0);
    	}
    	else
    		return new Point(0,rand.nextInt(height));
	}
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/fractaltree/ft_config"));
			String[] booleanInput=br.readLine().split("\\|");
			if(booleanInput[1].equals("T"))
				useImage=true;
			else useImage=false;
			if(booleanInput[3].equals("T"))
				allInOnce=true;
			else allInOnce=false;
			if(booleanInput[5].equals("T"))
				randBranchLength=true;
			else randBranchLength=false;
			if(booleanInput[7].equals("T"))
				randCurveBranch=true;
			else randCurveBranch=false;
			String[] input=br.readLine().split("\\|");
			startingSize=(int)Math.ceil(Double.parseDouble(input[0]));
			startingAngle=(int)Math.ceil(Double.parseDouble(input[1]));
			randBranch=(int)Math.ceil(Double.parseDouble(input[2]));
			branchAngle=(int)Math.ceil(Double.parseDouble(input[3]));
			branchLength=(int)Math.ceil(Double.parseDouble(input[4]));
			maxBranchLevel=(int)Math.ceil(Double.parseDouble(input[5]));
			branchSizeAdjust=Double.parseDouble(input[6]);
			branchLengthAdjust=Double.parseDouble(input[7]);
			appearLocation=(int)Math.ceil(Double.parseDouble(input[8]));
			addPointNumber=(int)Math.ceil(Double.parseDouble(input[9]));
			pointSpeed=(int)Math.ceil(Double.parseDouble(input[10]));
			endColorIndex=(int)Math.ceil(Double.parseDouble(input[11]));
			if(endColorIndex>6)
				endColorIndex=0;
			pauseNum=(int)Math.ceil(Double.parseDouble(input[12]));
			br.close();			
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/fractaltree/ft_config\" not found");
		}
	}
	
	@Override
	public void gameStart() {
		gameReset();
		loadSetting();
		if(useImage) {
			leaf= ImageEditor.loadImage("/image/game_image/fractal_tree/leaf.png");
			wood= ImageEditor.loadImage("/image/game_image/fractal_tree/wood.png");
		}
		dim=board.getSize();
		coordinate=new ArrayList<Point>();
		num=0;
		index=0;
		background = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
		branchCalculator=new BranchCalculator();
		t=new Thread(branchCalculator);
		t.start();
	};
	
	@Override
	public void gameReset() {
		resetScore();
		board.clearPoint();
	};
	
	
	@Override
	public void mouseMove(MouseEvent e) {
		if(clicked) {
			mouse.setLocation(e.getX(), e.getY());
			for(int i=0;i<100;i++) {
				if(num<index+1) {
					board.getPoints().add(new Point(mouse.getX(),mouse.getY()));
					num++;
				}else break;
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		clicked=true;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		clicked=false;
	}
	
	
	@Override
	public void roundAction() {
		ArrayList<Point> points=board.getPoints();
		if(allInOnce) {
			while(num<index+1) {
				board.addPoint();
				num++;
			}
		}else {
			for(int i=0;i<addPointNumber;i++) {
				if(num<index+1) {
					board.addPoint();
					num++;
				}else break;
			}
		}
		if(!branchCalculator.stopDrawing() || !t.isAlive()) {
			findDirection(points);
		}
	};

	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.drawString("**Fractal Tree**", (board.getWidth()/2)-40, 13);
		g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);
	};
	
	
	private void renderBackground(Point p, Color c) {
		if(board.getWidth()!=background.getWidth() || board.getHeight()!=background.getHeight()) {
			BufferedImage newBackground = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d=newBackground.createGraphics();	
			g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(),null);		
			background=newBackground;
		}
		Graphics2D g2d=background.createGraphics();
		g2d.setColor(c);
		if(useImage) {
			if(c!=board.getPointColor()) {
				p.setImage(leaf);
			}else {
				p.setImage(wood);
			}
		}
		p.drawWithImage(g2d);
	}
	
	private void findDirection(ArrayList<Point> points) {
		Iterator<Point> it1=points.iterator();
		Iterator<Point> it2=coordinate.iterator();
		while(it1.hasNext()) {
			Point p=(Point)it1.next();
			Point c=(Point)it2.next();
			if(hasStop(p.getX(),c.getX()) && hasStop(p.getY(),c.getY())) {
				it1.remove();
				it2.remove();
				index--;
				num--;
				Color color=(c.getColor()!=null)?c.getColor():board.getPointColor();
				renderBackground(p,color);
			}else {
				if(p.getDiameter()!=c.getDiameter())
					p.setDiameter(c.getDiameter());
				double dist=PathCalculator.findDist(p, c);
				if(dist<(pointSpeed/2)+1) {
					PathCalculator.straightPath(p, c, 0.5);
				}else {
					PathCalculator.straightPath(p, c,pointSpeed);
				}
			}
		}
	}
	
	private boolean hasStop(double a1, double a2) {
		double dif1=Math.abs(a1-a2);
		return dif1<0.5;
	}
	
	private class BranchCalculator implements Runnable{
		private boolean stopRunning;
		private boolean stopDrawing=true;
		
		@Override
		public void run() {
			Point root=new Point(dim.getWidth()/2,dim.getHeight()-(startingSize/2),startingSize);
			coordinate.add(root);
			findBranch(1,startingAngle, branchLength,root);
		}
		
		public void stopRunning() {
			stopRunning=true;
		}
		
		public boolean stopDrawing() {
			return stopDrawing;
		}
		
		//pause whenever coordinate size greater than define value. Resume when all coordinate is clear
		private void checkPause() {
			if(coordinate.size()>pauseNum) {
				stopDrawing=false;
				while(!coordinate.isEmpty() && !stopRunning) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				stopDrawing=true;
			}
		}
		
		private void findBranch(int branchLevel, int angle, int length, Point node) {
			checkPause();
			if(stopRunning) {
				return;
			}
			double pointSize=node.getDiameter()*branchSizeAdjust;
			if(pointSize<1)pointSize=1;
			
			double angleRad=Math.toRadians(angle);
			addFirstCoordinate(node,pointSize,angleRad);
			int stopIndex=(randBranchLength)?index+(int)((rand.nextInt(length)+length/4)/(pointSize/2))
					:index+(int)(length/(pointSize/2));
			while(index<=stopIndex){
				addCoordinate(coordinate.get(index),angleRad);
				if(branchLevel==maxBranchLevel)
					coordinate.get(index).setColor(c[endColorIndex]);
			}	
			if(++branchLevel<=maxBranchLevel && length>1) {
				Point newNode= coordinate.get(index);
				int newLength=(int)(length*branchLengthAdjust);
				if(newLength<1)
					newLength=1;
				int sp=(randCurveBranch)?rand.nextInt(3):0;
				switch(sp) {
				case 1:
					findBranch(branchLevel, PathCalculator.adjustAngle(angle-branchAngle),newLength, newNode);
					break;
				case 2:
					findBranch(branchLevel,	PathCalculator.adjustAngle(angle+branchAngle),newLength, newNode);
					break;
				default :
					findBranch(branchLevel, PathCalculator.adjustAngle(angle-branchAngle),newLength, newNode);
					findBranch(branchLevel,	PathCalculator.adjustAngle(angle+branchAngle),newLength, newNode);
				}
				if(randBranch>1) {
					int split=rand.nextInt(randBranch);
					for(int i=0;i<split;i++) 
						findBranch(branchLevel, PathCalculator.adjustAngle(angle+(rand.nextInt(100)-50)),newLength, newNode);			
				}
			}
		}
		
		private void addFirstCoordinate(Point node, double size, double angleRad) {
			Point p=PathCalculator.findAdjustAnglePoint(node,size/2,angleRad);
			p.setDiameter(size);
			coordinate.add(p);
			index++;
		}
		
		private void addCoordinate(Point node,double angleRad) {
			Point p=PathCalculator.findAdjustAnglePoint(node,node.getRadius(),angleRad);
			p.setDiameter(node.getDiameter());
			coordinate.add(p);
			index++;
		}	
		
		
	}
}
