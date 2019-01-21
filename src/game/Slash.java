/*
	**********************************
	File Name: Slash.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Slash game controlled by mouse motion
*/

package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import object.Point;
import object.Board;

class Slash extends GameSetter {
	private int trailSize;
	private int startingEnemyNumber;
	private int strokeSize;
	
	private LinkedList<Point> slashtrail;
	private LinkedList<Point> juice=new LinkedList<Point>();
	private int enemyNumber;
	
	public Slash(Board board) {
		super(board);
	}
	
	@Override
	public void gameClear() {
		if(!slashtrail.isEmpty())
			slashtrail.clear();
		if(!juice.isEmpty()) {
			juice.clear();
		}
	}
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
		double x=(rand.nextDouble());
		double y=(rand.nextDouble())*Math.sqrt(getLevel())*0.7;
		if(rand.nextBoolean())
			x*=-1;
    	return new Point(rand.nextInt(width),0-size,size,x,y);
	}
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/slash/sl_config"));
			br.readLine();
			String[] input=br.readLine().split("\\|");
			trailSize=(int)Math.ceil(Double.parseDouble(input[0]));
			startingEnemyNumber=(int)Math.ceil(Double.parseDouble(input[1]));
			strokeSize=(int)Math.ceil(Double.parseDouble(input[2]));
			slashtrail=new LinkedList<Point>();
			br.close();
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/slash/sl_config\" not found");
		}
	}
	
	@Override
	public void gameStart() {
		loadSetting();
		player=new Player(board.getWidth()/2,board.getHeight()/2,0);
		gameReset();
		for(int i=0; i<trailSize; ++i) {
			slashtrail.add(new Point(player.getX(),player.getY(),0));
		}
	};
	
	@Override
	public void gameReset() {
		resetScore();
		board.clearPoint();
		enemyNumber=startingEnemyNumber;
	};
	
	@Override
	public void gameOver() {
		int n=JOptionPane.showConfirmDialog(null,"Game Over!!\nYour Score: "+score+"\nYour Level: "+getLevel()+"\n\nDo You Want To Restart?","Restart?",JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.OK_OPTION) {
			gameReset();
		}else board.refresh();
	}
	
	@Override
	public void levelUp() {
		level++;
		enemyNumber=startingEnemyNumber+getLevel();
	};

	@Override
	public void mouseMove(MouseEvent e) {
		Iterator<Point> it=board.getPoints().iterator();
		while(it.hasNext()) {
			Point p=it.next();
			if(collideDetector.checkCollide(player,p)) {
				addJuice(slashtrail.get(0),p);
				it.remove();
			};
		}
		player.setLocation(e.getX(), e.getY());
	}
	
	@Override
	public void roundAction() {
		spawnEnemy();
		scoreCount();
		elementMove();
		setSlashtrail(player.getX(),player.getY());
		ArrayList<Point> points=board.getPoints();
		for(Point p: points){
			if(p.getY()+p.getRadius()>=board.getHeight()) {				
				gameOver();
				return;
			}
		}
		if(points.isEmpty() && enemyNumber==0) {
			levelUp();
		}
	};
	
	
	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.setColor(Color.red);
		g2d.drawString("**Slash Ninja**", (board.getWidth()/2)-40, 13);
		g2d.drawString("Your Score: "+getScore(), board.getWidth()-100, 13);
		g2d.drawString("Game Level: "+getLevel(), board.getWidth()-100, 26);
		g2d.setColor(Color.cyan);
		int stroke=	strokeSize;
		for(int i=0;i<trailSize;i++) {
			if(i%2!=0) {
				g2d.setStroke(new BasicStroke(stroke));
				stroke*=0.9;
				g2d.draw(new Line2D.Float((int)slashtrail.get(i).getX(), (int)slashtrail.get(i).getY(),(int)slashtrail.get(i-1).getX(), (int)slashtrail.get(i-1).getY()));
			}
		}
		for(Point p:juice) {
			p.drawWithColor(g2d);
		}
		
	};
	
	private void spawnEnemy() {
		if(enemyNumber>0) {
			if(rand.nextInt(30)%3==0) {
				board.addPoint();
				enemyNumber--;
			}
		}
	}
	
	private void setSlashtrail(double x, double y) {
		slashtrail.removeLast();
		slashtrail.push(new Point(x,y,3));
	}
	
	private void elementMove() {
		Iterator<Point> it=juice.iterator();
		while(it.hasNext()) {
			Point p=(Point)it.next();
			p.move();
			if(p.getY()>=board.getHeight())
				it.remove();
		}
	}
	
	private void addJuice(Point b, Point p) {
		for(int i=0; i<p.getRadius();++i) {
			double sx=p.getX()-p.getRadius();
			double mx=p.getX()+p.getRadius();
			double sy=p.getY()-p.getRadius();
			double my=p.getY()+p.getRadius();
			Point temp=new Point(sx+(mx-sx)*rand.nextDouble(),sy+(my-sy)*rand.nextDouble(),p.getRadius()*rand.nextDouble(),(2-(4*rand.nextDouble()))/3,(1+4*rand.nextDouble())/3);
			temp.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),rand.nextFloat()));
			juice.add(temp);
		}
	}
}
