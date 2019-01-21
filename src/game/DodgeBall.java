/*
	**********************************
	File Name: DogeBall.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Dodge Ball game controlled by mouse motion
*/

package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;
import object.Point;
import object.Board;

class DodgeBall extends GameSetter{
	
	private int startingEnemyNumber;
	private int playerSize;
	private int ballLimit;
	
	public DodgeBall(Board board) {
		super(board);
	}
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
		double x=(1-2*rand.nextDouble())*Math.sqrt(getLevel())+(0.5-rand.nextDouble());
		double y=(1-2*rand.nextDouble())*Math.sqrt(getLevel())+(0.5-rand.nextDouble());
    	boolean yy=rand.nextBoolean();
    	boolean xx=rand.nextBoolean();
    	
    	if(xx && yy) {
    		return new Point(width,rand.nextInt(height),size,x,y);
    	}
    	else if(!xx && !yy) {
    		return new Point(rand.nextInt(width),height,size,x,y);
    	}
    	else if(yy) {
    		return new Point(rand.nextInt(width),0,size,x,y);
    	}
    	else
    		return new Point(0,rand.nextInt(height),size,x,y);
	}
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/dodgeBall/db_config"));
			br.readLine();
			String[] input=br.readLine().split("\\|");
			playerSize=(int)Math.ceil(Double.parseDouble(input[0]));
			startingEnemyNumber=(int)Math.ceil(Double.parseDouble(input[1]));
			ballLimit=(int)Math.ceil(Double.parseDouble(input[2]));
			br.close();
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/dodgeBall/db_config\" not found");
		}
	}
	
	@Override
	public void gameStart() {
		loadSetting();
		player=new Player(board.getWidth()/2,board.getHeight()/2,playerSize);
		gameReset();
	};
	
	@Override
	public void gameReset() {
		resetScore();
		board.clearPoint();
		for(int i=0;i<startingEnemyNumber;++i) {
			board.addPoint();
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
	public void mouseMove(MouseEvent e) {
		player.setLocation(e.getX(), e.getY());
	}
	
	
	@Override
	public void roundAction() {
		scoreCount();
		if(collideDetector.testCollide(player,board.getPoints())){
			gameOver();
		}
		else {
			if(isLevelUp()) {
				levelUp();
			}
		}
	};
	
	@Override
	public void levelUp() {
		level++;
		for(int i=0;i<rand.nextInt(getLevel())+2;i++) {
			board.addPoint();
			if(board.getPoints().size()>ballLimit) {
				board.getPoints().remove(0);
			}
		}
		
	};
	
	
	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.setColor(Color.red);
		g2d.drawString("**Dodge Ball**", (board.getWidth()/2)-40, 13);
		g2d.drawString("Your Score: "+getScore(), board.getWidth()-100, 13);
		g2d.drawString("Game Level: "+getLevel(),board.getWidth()-100, 26);
		g2d.setColor(Color.ORANGE);
		player.drawShape(g2d);
	};
}
