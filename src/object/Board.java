/*
	**********************************
	File Name: Board.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Display all animation
	*Carry all functions to manipulate object
	*Carry all key and mouse listener
*/

package object;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.Timer;

import game.Game;
import game.GameSelector;
import smallest_shape.SmallestCircle;
import smallest_shape.SmallestRect;
import tool.CollisionDetector;
import tool.ImageEditor;
import tool.PathCalculator;
import tool.RefreshRateTester;


public class Board extends JPanel{
					
	//object
	private int num;
	private ArrayList<Point> points;
	private BufferedImage background;
	
	//setting value
	private Timer timer;
	private Point mouse=new Point(0,0);
	private boolean click;
	private int clickMethod;
	private boolean updateCircle, updateRect;
	private double pointSize;
	private double controlSpeed;
	private double maxSize,minSize;
	
	
	//tool
	private final CollisionDetector collisionDetector=new CollisionDetector();
	private final RefreshRateTester rr_tester=new RefreshRateTester();
	private final SmallestCircle circle=new SmallestCircle();
	private final SmallestRect rectangle=new SmallestRect();
	private final Wall wall=new Wall();
	private final Random rand=new Random();
	private final DecimalFormat df = new DecimalFormat("#.00"); 
	private Color color=Color.black;
	
	//component
	private JCheckBox[] checkBox;
	private JRadioButton[] radioBtn;
	private JSpinner[] spinner;
	
	//game
	private Game game;
	private final BufferedImage bg=ImageEditor.loadImage("/image/background.png");

	//Contruct timer, keylistener, mouselistener, and receive all functional component from main class
	public Board(JCheckBox[] checkBox, JRadioButton[] radioBtn, JSpinner[] spinner) {
		this.spinner=spinner;
		this.checkBox=checkBox;
		this.radioBtn=radioBtn;
		final ClickListener ml=new ClickListener();
		final KeyListener kl=new KeyListener();
		final ScrollListener sl=new ScrollListener();
		addMouseWheelListener(sl);
		addMouseListener(ml);
		addMouseMotionListener(ml);
		addKeyListener(kl);
		timer=new Timer(25,new MovingListener());
		points=new ArrayList<Point>();
	}
	
	public void setControlSpeed(double d) {
		controlSpeed=d;
	}
	
	public void setTimerDelay(int n) {
		timer.setDelay(n);
	}
	
	public void setRandomSizeRange(double min, double max) {
		maxSize=max;
		minSize=min;
	}
	
	public void setDotColor(Color c) {
		color=c;
	}
	
	public void setCollision(String n) {
		collisionDetector.setCollideMethod(n);
	}
	
	public double getControlSpeed() {
		return controlSpeed;
	}		
	
	public Color getPointColor() {
		return color;
	}
	
	public int getTimerDelay() {
		return timer.getDelay();
	}
	
	public ArrayList<Point> getPoints(){
		return points;
	}
	
	public int getNum() {
		return num;
	}
	
	public boolean isBloodCensor() {
		return checkBox[14].isSelected();
	}
	
	public void animateActive() {
		if(checkBox[6].isSelected()) {
			timer.start();
		}
		else {
			timer.stop();
			clearBackground();
			renderAllPoint(points);
		}
	}
	
	public void pause(boolean p) {
		checkBox[6].setSelected(!p);
		animateActive();
	}
	
	public Timer getTimer() {
		return timer;
	}

	
	//Active game mode
	public void gameMode(String type) {
		clearBoard();
		updateShape();
		game=GameSelector.selectGame(type,this);
		game.gameStart();
		if(!checkBox[6].isSelected())
			checkBox[6].doClick();
	}


	//refresh the board by clear all exist object and generate new points
	public void refresh() {
		clearPoint();
		clearBoard();
		clearBackground();
		wall.clearPoint();
		if(checkBox[0].isSelected()) {
			generatePoints();
			updateShape();
		}
		repaint();
		if(num>5000)
			System.gc();
	}
	
	
	//clear all object
	public void clearPoint() {
		num=0;			
		points.clear();
	}
	
	private void clearBoard() {
		if(game!=null) {
			game.gameClear();
			game=null;
		}
		collisionDetector.clear();
		circle.clearIfExist();
		rectangle.clearIfExist();
	}

	//increase each Point speed by iterate through every Points
	public void increaseSpeed() {
		points.forEach(p->{
			p.setXS(p.getXS()*1.5);
			p.setYS(p.getYS()*1.5);
		});
	}


	//decrease each Point speed by iterate through every Points
	public void decreaseSpeed() {
		points.forEach(p->{
			p.setXS(p.getXS()*0.5);
			p.setYS(p.getYS()*0.5);
		});
	}

	/*
	 * add new Point
	 * generate Point from game class if game is active
	 */	
	public void addPoint() {
		num++;	
		Point temp;
		if(checkBox[5].isSelected()) 
			pointSize=(int)spinner[2].getValue();
		else
			pointSize=minSize+(maxSize-minSize)*rand.nextDouble();
	    if(game!=null) {
	    	temp=game.getGeneratedPoint(getWidth(),getHeight(),pointSize);
	    }else {
	    	temp=new Point(mouse.getX(),mouse.getY(),pointSize,2-4*rand.nextDouble(),2-4*rand.nextDouble());  
			if(checkBox[1].isSelected()) {
				while(num>(int)spinner[1].getValue()) {
					points.remove(0);
					num--;
					clearBackground();
					renderAllPoint(points);
				}
			}			
	    }     
		if(!checkBox[10].isSelected()) {
			temp.setColor(colorSelector());
		}
	    points.add(temp);
		if(!timer.isRunning()) {
			renderPoint(temp);
		}
	}

	// generate random Points inside board and make sure all Points is close together
	private void generatePoints() {
			num=(int)spinner[0].getValue();
			int width=rand.nextInt(getWidth()/5)+(getWidth()/5);
			int height=rand.nextInt(getHeight()/5)+(getHeight()/5);		
			for(int i=0; i<num; ++i) {
				if(checkBox[5].isSelected())
					pointSize=(int)spinner[2].getValue();
				else
					pointSize=minSize+(maxSize-minSize)*rand.nextDouble();
				Point temp=new Point(rand.nextInt(width)+width,rand.nextInt(height)+height,pointSize,2-4*rand.nextDouble(),2-4*rand.nextDouble());
				if(!checkBox[10].isSelected()) {
					temp.setColor(colorSelector());
				}
				points.add(temp);
				if(!timer.isRunning()) {
					renderPoint(temp);
				}
			}
	}

	//allow update smallest enclosing circle and rectangle
	private void updateShape() {
		 updateRect=true;
		 updateCircle=true;
	}


	//generate Random Color
	private Color colorSelector() {
		if(checkBox[11].isSelected() || checkBox[12].isSelected() ||checkBox[13].isSelected() ) {
			int r=(checkBox[11].isSelected())?rand.nextInt(256):color.getRed();
			int g=(checkBox[12].isSelected())?rand.nextInt(256):color.getGreen();
			int b=(checkBox[13].isSelected())?rand.nextInt(256):color.getBlue();
			return new Color(r,g,b);
		}
		else
			return color;
	}


	// set each Point react when timer refresh each time
	private void pointsRunningTask() {
	    points.forEach(p->{
	    	if(click && radioBtn[1].isSelected()) {
	    		switch(clickMethod) {
	    		case 1:
	    			PathCalculator.straightPath(p, mouse, controlSpeed);
	    			break;
	    		case 2:
	    			if(p.getUseValue()==0) {
	    				p.setUseValue(1+rand.nextInt(160));
	    			}
	    			PathCalculator.circularPath(p, mouse, controlSpeed,p.getUseValue());
	    			break;
	    		case 3:
	    			PathCalculator.circularPath(p, mouse, controlSpeed,140);
	    			break;
	    		}
	    	}	
	    	if(!wall.isEmpty()) {
	    		wall.checkLine(p);
	    	}
	    	p.move(getWidth(),getHeight());
	    });
	}


	//listener for timer. Use for animating.
	private class MovingListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			rr_tester.test();
			if(num>0) {
				pointsRunningTask();  
				updateShape();				    
				if(checkBox[2].isSelected()){
					if(radioBtn[4].isSelected()) {
						collisionDetector.iterativeDetecting(points);
					}
					else collisionDetector.quadtreeDetecting(points,getWidth(), getHeight());
				}		
			}
			if(game!=null) {
				game.roundAction();
			}
		    repaint();
		}
	}
	
	
	
	private class KeyListener extends KeyAdapter{
		@Override
		public void keyPressed(KeyEvent e) {
			if(game!=null) {
				game.keyPressed(e);
			}
	    }
		@Override
		public void keyReleased(KeyEvent e) {
			if(game!=null) {
			   game.keyReleased(e);
			}
		}
	}

	private class ScrollListener implements MouseWheelListener{

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if(game==null) {
				if (e.getWheelRotation() < 0) {
					increaseSpeed();   
			    } 
				else{
					decreaseSpeed();
				}
			}
			else {
				game.mouseWheel(e);
			}
		}
	}
	
	private class ClickListener extends MouseAdapter{
		@Override
		public void mousePressed(MouseEvent e) {
	    	if(game!=null) {
	    		game.mouseClicked(e);
	    	}
	    	else {
				mouse.setLocation(e.getX(), e.getY());
		    	click=true;
			    if(radioBtn[0].isSelected()||radioBtn[2].isSelected()) {  //for add/draw points
			    	addPoint();
			    	updateShape();
			    	repaint();
			    }
			    else if(radioBtn[3].isSelected()) {  //for draw walls
			    	wall.addPoint(new Point(mouse.getX(),mouse.getY()));
			    	repaint();
			    }
			    else {  //for control points
			    	if(!checkBox[6].isSelected()) { //animate point if animating is not selected
			    		timer.start();
			    	}
			    	if(e.getButton()==MouseEvent.BUTTON1) {
			    		clickMethod=1;
			    	}
			    	else if(e.getButton() == MouseEvent.BUTTON2) {
			        	clickMethod=3;
			        }
			        else clickMethod=2;
			    }
	    	}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
	    	if(game!=null) {
	    		game.mouseReleased(e);
	    	}
	    	else {
				click=false;
		    	if(!checkBox[6].isSelected() && radioBtn[1].isSelected()) {
		    		timer.stop();
		    		clearBackground();
		    		renderAllPoint(points);
		    	}
				if(radioBtn[3].isSelected()) {
			    	wall.addPoint(new Point(mouse.getX(),mouse.getY()));
			    	repaint();
				}
	    	}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if(game!=null) {
				game.mouseMove(e);
			}
			else {
				if(click) {
					mouse.setLocation(e.getX(), e.getY());
					if(radioBtn[2].isSelected()) { //add point by dragging when draw is selected
					   addPoint();
					   updateShape();
					   repaint();
				    }
					else if(radioBtn[3].isSelected()) { 
					   repaint();
				   	}
				}
			}
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			if(game!=null ) {
				game.mouseMove(e);
			}
		}
	}
	
	
	//clear background paint when animating is off
	private void clearBackground() {
		if(background!=null) {
			Graphics2D g2d=background.createGraphics();
			g2d.setColor(this.getBackground());
			g2d.fillRect(0, 0, background.getWidth(), background.getHeight());
		}
		else background = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);
	}
	
	//set background color
	public void renderBackgroundColor(Color c) {
		this.setBackground(c);
		if(!timer.isRunning()) {
			clearBackground();
			renderAllPoint(points);
		}
	}
	
	//paint points into static image when animate is off
	private void renderPoint(Point p) {
		createBackgroundIfNotExit();
		testBackgroundSize();
		Graphics2D g2d=background.createGraphics();
		if(p.getColor()==null) {
			p.setColor(color);
		}
		p.drawWithColor(g2d);
	}
	
	//paint all poitns into static image when animating is off
	private void renderAllPoint(ArrayList<Point> points) {
		createBackgroundIfNotExit();
		testBackgroundSize();
		Graphics2D g2d=background.createGraphics();
	
		if(checkBox[10].isSelected()) {
			points.forEach(p->{
				if(g2d.getColor()!=color)
					g2d.setColor(colorSelector());
				p.drawShape(g2d);	
			});
		}
		else
			points.forEach(p->p.drawWithColor(g2d));
	}

	//test if background size is equal to board size
	private void testBackgroundSize() {
		if(getWidth()!=background.getWidth() || getHeight()!=background.getHeight()) {
			BufferedImage newBackground = new BufferedImage(getWidth(), getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d=newBackground.createGraphics();	
			g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(),null);		
			background=newBackground;
		}
	}
	
	//create background if not exist
	private void createBackgroundIfNotExit() {
		if(background==null) {
			background = new BufferedImage(getWidth(),getHeight(),BufferedImage.TYPE_INT_ARGB);	
		}
	}
	
	
	private void drawInformation(Graphics2D g2d) {
		g2d.drawString("# of Pts: "+num,10,13);
		g2d.drawString("Bll Size: "+df.format(pointSize), 10, 26);
		g2d.drawString("Ctrl Str: "+df.format(controlSpeed), 10, 39);
		g2d.drawString("Timer: "+timer.getDelay()+" ms", 10, 52);		
		g2d.drawString("Refresh Rate "+rr_tester.getRate(), 10, 65);
	}
	
	
	@Override
	public void paintComponent(Graphics g) {	
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		
		//draw grid
		if(checkBox[2].isSelected() && checkBox[8].isSelected()) {
			collisionDetector.drawTreeGrid(g2d);
		}	
		
		//draw game
		if(game!=null) {
			if(checkBox[9].isSelected())
				g2d.drawImage(bg,0,0,getWidth(),getHeight(),null);
			game.drawBack(g2d);
		}
		
		if(timer.isRunning() || game!=null) { 
			//draw point
			points.forEach(p->{
				if(p.hasImage()) {
					p.drawWithImageBigger(g2d);
				}
				else if(!checkBox[10].isSelected()) {
					p.drawWithColor(g2d);
				}
				else {
					if(g2d.getColor()!=color)
						g2d.setColor(colorSelector());
					p.drawShape(g2d);				
				}
			});
		}else if(background!=null) { //draw painted points image when animate is stop
			g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);
		}
		
		//rectangle
		if(checkBox[4].isSelected()&&rectangle!=null && points.size()>0) {
			if(updateRect) {
				rectangle.checkEdge(points);
				rectangle.findCenter();
				updateRect=false;
			}
			rectangle.drawRect(g2d);
			g2d.setColor(Color.red);
			for(Point re:rectangle.getEdge()){
				if(re!=null)
				re.drawShape(g2d);
			}
		}
		
		//circle
		if(checkBox[3].isSelected() && circle!=null && points.size()>0) {
			if(updateCircle) {
				circle.makeCircle(points);
				updateCircle=false;
			}
			circle.drawCircle(g2d);
			g2d.setColor(Color.YELLOW);
			for(Point ce:circle.getEdge()) {
				if(ce!=null)
				ce.drawShape(g2d);
			}			
		}
		
		//draw game front
		if(game!=null) {
			game.drawFront(g2d);
		}
		
		//info
		g2d.setColor(Color.red);
		drawInformation(g2d);
		
		//bound line
		g2d.setColor(Color.CYAN);
		g2d.setStroke(new BasicStroke(5,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		if(radioBtn[3].isSelected() && click &&  game==null) {
			g2d.draw(new Line2D.Double(wall.getLast().x, wall.getLast().y, mouse.getX(), mouse.getY()));
		}
		wall.drawWall(g2d);
		
	}
}

