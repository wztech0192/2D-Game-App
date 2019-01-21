/*
	**********************************
	File Name: GameSetter.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Abstract Class use for extending purpose
	*set up basic point counting method for all child class
*/

package game;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import object.Point;
import object.Board;

abstract class GameSetter implements Game {
	protected int score;
	protected int level;
	protected int levelLimit;
	protected Board board;
	protected Player player;
	
	public GameSetter(Board board) {
		this.board=board;
	}
	
	@Override
	public void resetScore() {
		score=0;
		level=1;
		levelLimit=250;
	}
	
	@Override
	public void scoreCount() {
		score+=level;
	};
	
	@Override
	public void addScore(int p) {
		score+=p;
	}
	
	@Override
	public boolean isLevelUp() {
		if(score>levelLimit) {
			levelLimit+=250*level;
			return true;
		}
		return false;
	}
	
	@Override
	public int getLevel() {
		return level;
	};	
	
	@Override
	public int getScore() {
		return score;
	};
	
	@Override
	public Player getPlayer() {
		return player;
	}
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
		return null;
	}
	
	@Override
	public void loadSetting() {
	}
	@Override
	public void gameStart() {
	}
	@Override
	public void gameReset() {
	}
	@Override
	public void gameOver() {	
	}
	@Override
	public void gameClear() {
	}
	@Override
	public void levelUp() {
	}
	@Override
	public void playerMove() {
	}
	@Override
	public void keyPressed(KeyEvent e) {
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
	@Override
	public void mouseWheel(MouseWheelEvent e) {
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}
	@Override
	public void mouseReleased(MouseEvent e) {
	};
	@Override
	public void mouseMove(MouseEvent e) {
	};
	@Override
	public void roundAction() {
	}
	@Override
	public void drawBack(Graphics2D g2d) {
	}
	@Override
	public void drawFront(Graphics2D g2d) {
	}
}
