/*
	**********************************
	File Name: Snake.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Snake game controlled by keyboard w,a,s,d
*/

package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import object.Point;
import object.Board;

class Snake extends GameSetter{
	private ArrayList<Point> tail=new ArrayList<Point>();
	
	private int tailSize;
	
	private int tailIncreaseNumber;
	private double snakeSize;
	
	private int pi=1;
	private int speed=3;
	
	public Snake(Board board) {
		super(board);
	}
	
	@Override
	public void gameClear() {
		if(!tail.isEmpty())
			tail.clear();
	}
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
    	return new Point(rand.nextInt(width),rand.nextInt(height),size,0,0);
	}
	
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/snake/sk_config"));
			br.readLine();
			String[] input=br.readLine().split("\\|");
			tailIncreaseNumber=(int)Math.ceil(Double.parseDouble(input[0]));
			snakeSize=(int)Math.ceil(Double.parseDouble(input[1]));
			br.close();
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/snake/sk_config\" not found");
		}
	}
	
	
	@Override
	public void gameStart() {
		loadSetting();
		player=new Player(board.getWidth()/2,board.getHeight()/2,snakeSize);
		gameReset();
	};
	
	@Override
	public void gameReset() {
		resetScore();
		player.stopMove();
		tailSize=0;
		tail.clear();
		board.clearPoint();
		for(int i=0;i<3;++i) {
			board.addPoint();
		}
		
		for(int i=0; i<8;++i) {
			tailSize++;
			if(i<4) {
				snakeSize*=1.1;
			}
			else 
				snakeSize/=1.1;
			tail.add(new Point(player.getX(),player.getY(),snakeSize,speed,0));
		}
	};
	
	@Override
	public void gameOver() {
		int n=JOptionPane.showConfirmDialog(null,"Game Over!!\nYour Score: "+score+"\nYour Level: "+getLevel()+"\n\nDo You Want To Restart?","Restart?",JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.OK_OPTION) {
			gameReset();
		}else board.refresh();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_A:
        	getPlayer().setMoveLeft(true);
        	break;
        case KeyEvent.VK_D:
        	getPlayer().setMoveRight(true);
        	break;
        }
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		 switch (e.getKeyCode()) {
	        case KeyEvent.VK_A:
	        	getPlayer().setMoveLeft(false);
	        	break;
	        case KeyEvent.VK_D:
	        	getPlayer().setMoveRight(false);
	        	break;
		    }
	}
	
	@Override
	public void playerMove() {
		if(player.getMoveLeft()) {
			if(pi==0) {
				pi=36;
			}
			else pi--;
			double a=(pi*Math.PI)/18;
			player.setXS(speed*Math.cos(a));
			player.setYS(speed*Math.sin(a));
			
		}else if(player.getMoveRight()){
			if(pi==36) {
				pi=1;
			}
			else pi++;
			double a=(pi*Math.PI)/18;
			player.setXS(speed*Math.cos(a));
			player.setYS(speed*Math.sin(a));
		}
		moveTail();
		player.move(board.getWidth(),board.getHeight());
	}
	
	@Override
	public void roundAction() {
		if(!board.isFocusOwner()) {
			board.requestFocus();
		}
		playerMove();
		ArrayList<Point> points=board.getPoints();
		for(Point p:points) {
			if(collideDetector.checkCollide(player,p)){
				points.remove(p);
				levelUp();
				break;
			}
		}
		if(points.isEmpty()){
			for(int i=0;i<rand.nextInt(3)+getLevel();i++) {
				board.addPoint();
			}
		}
		if(getLevel()>1) {
			if(collideDetector.testCollide(player,tail.subList(10, tail.size()))){
				gameOver();
			}
		}
	};
	
	@Override
	public void levelUp() {
		level++;
		addScore(500);
		Point last;
		last=tail.get(tail.size()-1);
		for(int i=0;i<tailIncreaseNumber;++i) {
			tail.add(new Point(last.getX(),last.getY(),player.getDiameter(),0,0));
		}
		tailSize+=tailIncreaseNumber;
	};
	
	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.drawString("**Snake**", (board.getWidth()/2)-40, 13);
		g2d.drawString("Your Score: "+getScore(), board.getWidth()-100, 13);
		g2d.drawString("Game Level: "+getLevel(), board.getWidth()-100, 26);
		g2d.drawString("Tail Length: "+tailSize, board.getWidth()-100, 39);
		g2d.setColor(Color.green.darker());
		for(Point p:tail) {
			p.drawShape(g2d);
		}
		player.drawShape(g2d);

	};
	
	
	private void moveTail() {
		for(int i=tail.size()-1;i>=1; --i) {
			tail.get(i).setLocation(tail.get(i-1).getX(), tail.get(i-1).getY());
		}
		tail.get(0).setLocation(player.getX(), player.getY());
	}
}
