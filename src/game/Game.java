/*
	**********************************
	File Name: Game.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Game Interface
*/

package game;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Random;
import object.Point;
import tool.CollisionDetector;

public interface Game {
	final static CollisionDetector collideDetector=new CollisionDetector();
	final static Random rand=new Random();
	abstract void scoreCount();
	abstract void resetScore();
	abstract void addScore(int p);
	abstract boolean isLevelUp();
	abstract int getLevel();	
	abstract int getScore();
	abstract Player getPlayer();
	abstract Point getGeneratedPoint(int width, int height, double size);
	abstract void gameStart();
	abstract void gameReset();
	abstract void gameOver();
	abstract void gameClear();
	abstract void loadSetting();
	abstract void levelUp();
	abstract void playerMove();
	abstract void keyPressed(KeyEvent e);
	abstract void keyReleased(KeyEvent e);
	abstract void mouseWheel(MouseWheelEvent e);
	abstract void mouseClicked(MouseEvent e);
	abstract void mouseReleased(MouseEvent e);
	abstract void mouseMove(MouseEvent e);
	abstract void roundAction();
	abstract void drawBack(Graphics2D g2d);
	abstract void drawFront(Graphics2D g2d);
}
