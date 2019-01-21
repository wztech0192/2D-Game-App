/*
	**********************************
	File Name: Sword.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Some type of sword&shield game control by key and mouse
*/

package game;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import object.Point;
import object.Board;
import tool.ImageEditor;
import tool.PathCalculator;

class Sword extends GameSetter {
	
	
	private ConcurrentLinkedQueue<Point> blood=new ConcurrentLinkedQueue<Point>();
	private ConcurrentLinkedQueue<Point> sparks=new ConcurrentLinkedQueue<Point>();
	private ConcurrentLinkedQueue<Point> enemyArrows=new ConcurrentLinkedQueue<Point>();
	private ParticleHandler particleHandler;
	private BufferedImage background;
	
	private SwordPlayer player;
	private ArrayList<Bot> bot=new ArrayList<Bot>();
	
	private BufferedImage swordImage;
	private BufferedImage swordHoldedImage;
	private BufferedImage shieldImage;
	private BufferedImage playerImage;
	private BufferedImage playerHoldedImage;
	private BufferedImage playerNoSwordImage;
	private BufferedImage arrowImage;
	private BufferedImage[] enemyImage;
	
	private Point mouse=new Point(0,0);

	
	private double defaultPlayerSize, playerSize;
	private double defaultMovementSpeed, movementSpeed;
	private double defaultSwordSize, swordSize;
	private double defaultSwordSpeed,swordSpeed;
	private int defaultHealth,health;
	private int wait,waitTime;
	private int startBotNumber;
	private double throwSpeed;
	
	private int startingLevel;
	private int kills;
	private int bloodTravel;
	private int perk;
	private double attackRadius;
	private int slashTrailSize;
	private int strokeSize;
	private int startingEnemyNumber;
	private double swordFlyDistance;
	private int enemySpawnNumber;
	private boolean useImage;
	private boolean clickable;
	private StatPanel statPane;

	private final Color bloodColor=new Color(0.5f,0f,0f,.8f);
	private final Color deadBloodColor=new Color(0.5f,0f,0f,.5f);
	private final Color attackRadiusColor=new Color(0.0f,0f,0.8f,.2f);
	
	public Sword(Board board) {
		super(board);
	}
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/sword/sw_config"));
			if(br.readLine().split("\\|")[1].equals("T")) {
				useImage=true;
			}else useImage=false;
			String[] input=br.readLine().split("\\|");
			defaultPlayerSize=Double.parseDouble(input[0]);
			defaultSwordSize=Double.parseDouble(input[1]);
			defaultSwordSpeed=Double.parseDouble(input[2]);
			defaultHealth=(int)Math.ceil(Double.parseDouble(input[3]));
			defaultMovementSpeed=Double.parseDouble(input[4]);
			startingEnemyNumber=(int)Math.ceil(Double.parseDouble(input[5]));
			waitTime=(int)Math.ceil(Double.parseDouble(input[6]));
			bloodTravel=(int)Math.ceil(Double.parseDouble(input[7]));
			strokeSize=(int)Math.ceil(Double.parseDouble(input[8]));
			slashTrailSize=(int)Math.ceil(Double.parseDouble(input[9]));
			startBotNumber=(int)Math.ceil(Double.parseDouble(input[10]));
			startingLevel=(int)Math.ceil(Double.parseDouble(input[11]));
			br.close();
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/sword/sw_config\" not found");
		}
	}
	
	@Override
	public void gameStart() {
		board.clearPoint();
		loadSetting();
		if(useImage) {
			loadImage();
		}
		level=100;
		player=new SwordPlayer(board.getWidth()/2,board.getHeight()/2,1);
		player.setTargetPoint(mouse);
		clickable=false;
		background = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
		playerSize=defaultPlayerSize;
		movementSpeed=defaultMovementSpeed*2;
		swordSize=defaultSwordSize;
		swordSpeed=defaultSwordSpeed*2;
		throwSpeed=swordSpeed*2;
		attackRadius=playerSize*4;	
		health=defaultHealth;
		swordFlyDistance=attackRadius*4/throwSpeed;	
		for(int i=0;i<startBotNumber;++i) {
			int x=rand.nextInt(board.getWidth());
			int y=rand.nextInt(board.getHeight());
			bot.add(new Bot(x,y,playerSize,1,(int)defaultSwordSpeed));
		}
		Thread particleThread=new Thread(particleHandler=new ParticleHandler());
		particleThread.start();
	};
	
	private void loadImage() {
		swordImage=ImageEditor.loadImage("/image/game_image/sword_image/sword.png");
		swordHoldedImage=ImageEditor.loadImage("/image/game_image/sword_image/swordHolded.png");
		shieldImage=ImageEditor.loadImage("/image/game_image/sword_image/shield.png");
		playerImage=ImageEditor.loadImage("/image/game_image/sword_image/player.png");
		playerHoldedImage=ImageEditor.loadImage("/image/game_image/sword_image/playerHolded.png");
		playerNoSwordImage=ImageEditor.loadImage("/image/game_image/sword_image/playerNoSword.png");
		arrowImage=ImageEditor.loadImage("/image/game_image/sword_image/arrow.png");	
		enemyImage=new BufferedImage[3];
		enemyImage[0]=ImageEditor.loadImage("/image/game_image/sword_image/monster1.png");
		enemyImage[1]=ImageEditor.loadImage("/image/game_image/sword_image/monster2.png");
		enemyImage[2]=ImageEditor.loadImage("/image/game_image/sword_image/archer.png");
		
	}
	
	private void realGameStart(double x,double y, double angle) {
		player.setLocation(x,y);
		gameReset();
		player.setAngle(Math.toRadians(angle));
		player.adjustBothHandAngle();
		clickable=true;
		statPane=new StatPanel();
	}
	
	@Override
	public void gameReset() {
		resetScore();
		level=startingLevel;
		if(clickable) {
			background = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
		}
		player.setClick(false);
		kills=0;
		perk=1;
		wait=waitTime;
		playerSize=defaultPlayerSize;
		movementSpeed=defaultMovementSpeed;
		swordSize=defaultSwordSize;
		swordSpeed=defaultSwordSpeed;
		health=defaultHealth;
		throwSpeed=swordSpeed*2;
		player.reset();
		attackRadius=playerSize*4;	
		swordFlyDistance=attackRadius*4/throwSpeed;		
		if(!bot.isEmpty()) {
			bot.clear();
		}
		if(!blood.isEmpty()) {
			blood.clear();
		}
		if(!enemyArrows.isEmpty()) {
			enemyArrows.clear();
		}
		board.clearPoint();
		enemySpawnNumber=startingEnemyNumber*getLevel();
	}
	
	@Override
	public void gameClear() {
		particleHandler.stop();
		if(!bot.isEmpty()) {
			bot.forEach(b->b.setTarget(null));
			bot.clear();
		}
		if(!blood.isEmpty()) {
			blood.clear();
		}
		if(!enemyArrows.isEmpty()) {
			enemyArrows.clear();
		}
		if(!sparks.isEmpty()) {
			sparks.clear();
		}
	}
	
	@Override
	public void gameOver() {
		int n=JOptionPane.showConfirmDialog(null,"Game Over!!\nYour Score: "+score+"\nYour Level: "+getLevel()+"\n\nDo You Want To Restart?","Restart?",JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.OK_OPTION) {
			gameReset();
		}else { 
			background=null;
			if(!blood.isEmpty()) {
				blood.clear();
			}
			if(!enemyArrows.isEmpty()) {
				enemyArrows.clear();
			}
			board.refresh();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_W:
        	player.setMoveUp(true);
        	break;
        case KeyEvent.VK_S:
        	player.setMoveDown(true);
        	break;
        case KeyEvent.VK_A:
        	player.setMoveLeft(true);
        	break;
        case KeyEvent.VK_D:
        	player.setMoveRight(true);
        	break;
        case KeyEvent.VK_P:
        	if(perk>0) {
        		board.pause(true);
        		statPane.display();
        		board.pause(false);
        	}
        	break;
        case KeyEvent.VK_E:
        	player.adjustLeft(true);
        	break;
        case KeyEvent.VK_Q:
        	player.adjustRight(true);
        	break;
        case KeyEvent.VK_SPACE:
        	if(!clickable){
        		realGameStart(bot.get(0).getX(),bot.get(0).getY(),bot.get(0).getAngle());
        		bot.clear();
        	}
	    }
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		 switch (e.getKeyCode()) {
	        case KeyEvent.VK_W:
	        	player.setMoveUp(false);
	        	break;
	        case KeyEvent.VK_S:
	        	player.setMoveDown(false);
	        	break;
	        case KeyEvent.VK_A:
	        	player.setMoveLeft(false);
	        	break;
	        case KeyEvent.VK_D:
	        	player.setMoveRight(false);
	        	break;
	        case KeyEvent.VK_E:
	        	player.adjustLeft(false);
	        	break;
	        case KeyEvent.VK_Q:
	        	player.adjustRight(false);
	        	break;
		    }
	}

	@Override
	public void levelUp() {
		wait=waitTime;
		level++;
		if(level%8==0) {
			Bot b=new Bot(0,0,playerSize,50-level/2,Math.sqrt(level)/2);
			b.setTarget(player);
			bot.add(b);
		}
		else enemySpawnNumber=startingEnemyNumber*getLevel();
		
	};

	@Override
	public void mouseMove(MouseEvent e) {
		mouse.setLocation(e.getX(), e.getY());
		player.setAngle(mouse);
		player.adjustBothHandAngle();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(clickable) {
			mouse.setLocation(e.getX(), e.getY());
			if(e.getButton()==MouseEvent.BUTTON1) {
				player.setAttackType(2);
			}
			else if(e.getButton()==MouseEvent.BUTTON2){
				player.setAttackType(0);
	        }else {
	        	player.setAttackType(1);
	        }
			player.setClick(true);
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		player.setClick(false);
	}
	
	@Override
	public void roundAction() {
		if(!board.isFocusOwner()) {
			board.requestFocus();
		}
		player.roundAction();
		if(player.getUseValue()>=health) {  //game over if player get hit over 10 times
			gameOver();
			return;
		}
		if(!bot.isEmpty()) {
			duelAction();
			if(!clickable) {
				if(bot.size()==1) {
					realGameStart(bot.get(0).getX(),bot.get(0).getY(),bot.get(0).getAngle());
					bot.clear();
					return;
				}
			}
		}else {
			if(!waiting()) {
				spawnEnemy();
				ArrayList<Point> enemy=board.getPoints();
				enemyMeleeHit(enemy);
				if(player.isSwordout()){
					swordHitEnemy(enemy,player.getSword());
				}
				enemyMove(enemy);
				if(enemySpawnNumber<=0 && enemy.isEmpty()) {
					levelUp();
				}
			}
		}
	};
	
	
	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
		boolean yy=rand.nextBoolean();
		boolean xx=rand.nextBoolean();
		Point enemy;
		if(xx && yy) {
			enemy= new Point(width,rand.nextInt(height),size);
		}
		else if(!xx && !yy) {
			enemy= new Point(rand.nextInt(width),height,size);
		}
		else if(yy) {
			enemy= new Point(rand.nextInt(width),0,size);
		}
		else
			enemy= new Point(0,rand.nextInt(height),size);
				
		int enemyType;
		if(getLevel()<4) {
			enemyType=1;
		}else {
			enemyType=rand.nextInt(3);
		}
		
		switch(enemyType) {
		case 0:
	    	enemy.setUseValue(30+rand.nextInt(100)); //random enemy type
			break;
		}
		if(useImage) {
			if(enemyType!=0) {
				enemy.setImage(enemyImage[rand.nextInt(2)]);
			}
			else {
				enemy.setImage(enemyImage[2]);
			}
		}	
		return enemy;
	}

	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);
		g2d.setColor(Color.cyan);
		g2d.drawString("**Sword**", (board.getWidth()/2)-40, 13);
		if(clickable) {
			g2d.drawString("Player Score: "+getScore(), board.getWidth()-100, 13);
			g2d.drawString("Game Level: "+getLevel(), board.getWidth()-100, 26);
			g2d.drawString("# oF Kills: "+kills, board.getWidth()-100, 52);
			g2d.drawString("Player HP: "+(health-player.getUseValue()), board.getWidth()-100, 65);
			if(perk>0) {
				g2d.drawString("Press \"P\" to Upgrade Your Stats", (board.getWidth()/2)-80, 26);
			}
			if(wait>0) {
				g2d.drawString("New Wave is COMING IN "+ wait/40, (board.getWidth()/2)-70, 39);
			}	
			g2d.setColor(attackRadiusColor);
			g2d.fill(new Ellipse2D.Double(player.getX()-attackRadius, player.getY()-attackRadius, attackRadius*2,attackRadius*2));
		}
		else {
			g2d.drawString("Press \"Space\" to SKIP", (board.getWidth()/2)-80, 26);
		}
		g2d.setColor(Color.red);
		enemyArrows.forEach(p->p.drawWithImageBigger(g2d));
		g2d.setColor(Color.DARK_GRAY);
		player.drawSword(g2d);
		bot.forEach(b->b.drawSword(g2d));
	};
	
	@Override
	public void drawFront(Graphics2D g2d) {	
		g2d.setColor(Color.DARK_GRAY);
		player.getLeftHand().drawWithImage(g2d);
		player.drawWithImageBigger(g2d);
		bot.forEach(b->{
			b.getLeftHand().drawWithImage(g2d);
			b.drawWithImageBigger(g2d);
		});	
		blood.forEach(p->p.drawWithColor(g2d));
		sparks.forEach(p->p.drawWithColor(g2d));
	}
	
	
	
	private void renderBackground(Point p, Color c) {
		if(board.getWidth()!=background.getWidth() || board.getHeight()!=background.getHeight()) {
			BufferedImage newBackground = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d=newBackground.createGraphics();	
			g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(),null);		
			background=newBackground;
		}
		Graphics2D g2d=background.createGraphics();
		if(c==deadBloodColor && board.isBloodCensor()) 
			g2d.setColor(new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat(),0.5f));
		else
			g2d.setColor(c);
		p.drawShape(g2d);
	}
	
	private boolean waiting() {
		if(wait>0) {
			wait--;
			return true;
		}
		else return false;
	}
	
	private void duelAction() {
		Iterator<Bot> it=bot.iterator();
		while(it.hasNext()) {
			Bot b=it.next();
			if(b.getUseValue()>=health) {
				b.setTarget(null);
				it.remove();
			}else {
				if(clickable) {
					playerDuel(player,b);
				}else {
					b.findTarget();
				}
				if(b.getTarget()==null) {
					continue;
				}
				b.roundAction();
				botDuel(b,b.getTarget());
			}
		}
	}
	
	private void botDuel(Bot b, SwordPlayer target) {
		if(target.getUseValue()>=health) {
			b.setTarget(null);
			return;
		}
		if(b.clicked) {
			double dist=PathCalculator.findDist(b.getSword(), target.getLeftHand());   
			if(dist<=target.getLeftHand().getDiameter()+target.getDiameter()){
				if(!target.block(b.getSword(), 1,dist)) {
					if(collideDetector.checkCollide(b.getSword(),target)){
						addBlood(target,b.getSword());	
						renderBackground(target,deadBloodColor);
						target.addUseValue();
						b.clicked=false;
					}
				}else {
					b.clicked=false;
				}
			}
		}
	}
	
	private void playerDuel(SwordPlayer b, SwordPlayer target) {
		if(b.throwSword) {
			double dist=PathCalculator.findDist(b.getSword(), target.getLeftHand());   
			if(dist<=target.getLeftHand().getDiameter()+target.getDiameter()){
				if(!target.block(b.getSword(),1,dist)) {
					if(collideDetector.checkCollide(b.getSword(),target)){
						addBlood(target,b.getSword());	
						target.addUseValue();
						if(target.getUseValue()>=health) {
							addBlood(b,target.getSword());	
							renderBackground(target,deadBloodColor);;
						}
					}
				}else {
					b.throwSword=false;
				}
			}
		}
		else if(b.clicked) {
			double dist=PathCalculator.findDist(b.getSword(), target.getLeftHand());   
			if(dist<=target.getLeftHand().getDiameter()+target.getDiameter()){
				if(!target.block(b.getSword(),1,dist)) {
					if(collideDetector.checkCollide(b.getSword(),target)){
						addBlood(target,b.getSword());	
						target.addUseValue();
						if(target.getUseValue()>=health) {
							addBlood(b,target.getSword());	
							renderBackground(target,deadBloodColor);;
						}
					}
				}else {
					b.clicked=false;
				}
			}
		}
	}
	

	
	private void swordHitEnemy(ArrayList<Point> points, Point sword) {
		for(Point p:points) {
			if(collideDetector.checkCollide(sword, p)) {
				kills++;
				if(kills%10==0) {
					perk++;
				}
				points.remove(p);
				renderBackground(p,deadBloodColor);
				addBlood(sword,p);		
				addScore(250);
				return;
			}
		}
	}
	
	private void spawnEnemy() {
		if(enemySpawnNumber>0) {
			if(rand.nextInt(30)%3==0) {
				board.addPoint();
				enemySpawnNumber--;
			}
		}
	}

	private void enemyMove(ArrayList<Point> points) {
		for(int i=0;i<points.size();++i){
			Point p=points.get(i);
			p.setAngle(player);
			double speed=Math.sqrt(getLevel())/2;
			
			if(p.getUseValue()==0) { //for melee enemy
				if(p.getLifeSpan()<=0) {
					p.setLifeSpan(rand.nextInt(50));
					p.setMoveDirection(rand.nextBoolean());
					p.setMoveAngle(rand.nextInt(100)+10);
				}		
			}
			else { //for range enemy
				if(p.getLifeSpan()<=0) {
					p.setLifeSpan(rand.nextInt(50));
					p.setMoveDirection(rand.nextBoolean());
					p.setMoveAngle(rand.nextInt(41)+160);
				}		
				if(p.getUseValue()==1) {
					enemyShootArrow(p,speed*1.5);
					p.setUseValue(200+rand.nextInt(300));
				}
				p.reduceUseValue();
			}
		
			if(p.getMoveDirection()) {
				PathCalculator.circularPath(p, player, speed, p.getMoveAngle());
			}
			else {
				PathCalculator.circularPath(p, player, -speed ,-p.getMoveAngle());
			}
			p.reduceLifeSpan();
		}
	}
	
	private void enemyShootArrow(Point p,double speed) {
		Point arrow=new Point(p.getX(),p.getY(),p.getRadius());
		PathCalculator.straightPath(arrow, player, speed); 
		if(useImage) {
			arrow.setImage(arrowImage);
			arrow.setAngle(player);
		}
		enemyArrows.add(arrow);
	}
	

	
	private void enemyMeleeHit(ArrayList<Point> enemy) {
		for(Point p:enemy) {
			double dist=PathCalculator.findDist(p, player.getLeftHand());   
			if(dist<=player.getLeftHand().getDiameter()+player.getDiameter()){
				if(!player.block(p,5,dist)) {
					if(p.getUseValue()==0 ) { //melee enemy type only
						if(collideDetector.checkCollide(player,p)) {
							addBlood(p,player);
							player.addUseValue();
						}
					}
				}
			}
		}
	}

	
	private void addBlood(Point b, Point p) {
		double size=p.getRadius();
		if(size>30) {
			size=30;
		}
		for(int i=0; i<size;++i) {
			double sx=p.getX()-p.getRadius();
			double mx=p.getX()+p.getRadius();
			double sy=p.getY()-p.getRadius();
			double my=p.getY()+p.getRadius();
			Point temp=new Point(sx+(mx-sx)*rand.nextDouble(),sy+(my-sy)*rand.nextDouble(),size*rand.nextDouble(),b.getXS()*rand.nextDouble()*0.8,b.getYS()*rand.nextDouble()*0.8);
			if(board.isBloodCensor()) {
				temp.setColor(new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
			}else temp.setColor(bloodColor);
			blood.add(temp);
		}
	}
	
	
	private void addSparks(Point p, Player player) {
		double distance=p.getRadius()+player.getLeftHand().getRadius();
		double d1=p.getRadius()/distance;
		double d2=(distance-p.getRadius())/distance;
		double x=(p.getX()*d2)+(player.getLeftHand().getX()*d1);
		double y=(p.getY()*d2)+(player.getLeftHand().getY()*d1);
		double size=p.getRadius();
		if(size>20) {
			size=20;
		}
		for(int i=0; i<size;++i) {
			Point sp=new Point(x,y,size*rand.nextDouble(),1-3*rand.nextDouble(),1-3*rand.nextDouble());
			sp.setColor(new Color(1,0.4f+0.6f*rand.nextFloat(),0,rand.nextFloat()));
			sparks.add(sp);
		}
	}
	
	private class SwordPlayer extends Player{
		protected Point targetPoint;
		protected Point sword;
		protected int attackType=0;
		protected boolean throwSword;
		protected boolean swordout;
		protected boolean clicked;
		protected double shieldAngle;
		protected double shieldAdjustAngle=30;
		protected boolean shieldLeft, shieldRight;
		protected LinkedList<Point> slashTrail=new LinkedList<Point>();
		
		private SwordPlayer(double x, double y, double size) {
			super(x, y, size);			
			setup();
			if(useImage) {
				setImage(playerHoldedImage);
				getSword().setImage(swordHoldedImage);
				getLeftHand().setImage(shieldImage);
			}
		}
		
		protected void setup() {
			createSword();
			createLeftHand();
			getLeftHand().setDefaultLifeSpanAndLifeSpan(5);  //block sparks cool down
			createRightHand();
			adjustBothHandAngle();
		}
		
		
		
		protected void reset() {
			throwSword=false;
			swordout=false;
			setDiameter(playerSize);
			sword.setDiameter(swordSize);
			updateHand();
			setUseValue(0);
		}

		protected void adjustLeft(boolean left) {
			shieldLeft=left;
		}
		protected void adjustRight(boolean right) {
			shieldRight=right;
		}
		
		protected void setAttackType(int i) {
			attackType=i;
		}
		
		protected void setTargetPoint(Point path) {
			targetPoint=path;
		//	if(path!=null)
		//		setAngle(targetPoint);
		}
		
		protected void setClick(boolean c) {
			clicked=c;
		}
		
		protected void drawSword(Graphics2D g2d) {
			if(swordout) {
				int stroke=	strokeSize;
				for(int i=0;i<slashTrailSize;i++) {
					g2d.setStroke(new BasicStroke(stroke,BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					stroke*=0.9;
					if(i%2!=0) {
						g2d.draw(new Line2D.Float((int)slashTrail.get(i).getX(), (int)slashTrail.get(i).getY(),(int)slashTrail.get(i-1).getX(), (int)slashTrail.get(i-1).getY()));
					}
				}
				sword.drawWithImageBigger(g2d);
			}	
		}
		
		protected void roundAction() {
			moveAction();
			attackAction();
			shieldAdjust();
			if(swordout) {
				sword.move(board.getWidth(),board.getHeight());
				setslashTrail(sword.getX(),sword.getY());
			}
			if(getLeftHand().getLifeSpan()>0) {
				getLeftHand().reduceLifeSpan();
			}
		}
		
		protected void moveAction() {
			if(getMoveUp()) {
				double newY=getY()-movementSpeed;
				if(newY>0.0)
					setY(newY);
			}
			if(getMoveDown()) {
				double newY=getY()+movementSpeed;
				if(newY<board.getHeight())
					setY(newY);
			}
			if(getMoveLeft()) {
				double newX=getX()-movementSpeed;
				if(newX>0.0)
					setX(newX);
				
			}if(getMoveRight()){
				double newX=getX()+movementSpeed;
				if(newX<board.getWidth())
					setX(newX);
			}
			if(getMoveRight()||getMoveLeft()||getMoveDown()||getMoveUp()) {
				setHandPosition(getRightHand(), getAngle()-110);
				setHandPosition(getLeftHand(),shieldAngle);
			}
		}
		
		protected void createSword() {
			sword=new Point(getX(),getY(),defaultSwordSize);
			for(int i=0; i<slashTrailSize; ++i) {
				slashTrail.add(new Point(sword.getX(),sword.getY(),0));
			}
		}
		
		protected Point getSword() {
			return sword;
		}
		
		protected void adjustBothHandAngle() {
			shieldAngle=getAngle()-shieldAdjustAngle;
			if(shieldAngle>180) {
				shieldAngle-=180;
			}else shieldAngle+=180;
	      	setHandPosition(getLeftHand(),shieldAngle);
	     	setHandPosition(getRightHand(), getAngle()-110);
	     	getLeftHand().setAngle(Math.toRadians(shieldAngle));
		}
		
		protected boolean block(Point p, double back, double dist) {
			Point shield=this.getLeftHand();
			if(collideDetector.checkCollide(shield,p,dist)) {
				if(shield.getLifeSpan()<=0) {
					Sword.this.addSparks(p,this);
					shield.resetLifeSpan();
				}
				p.setX(p.getX()-p.getXS()*back);
				p.setY(p.getY()-p.getYS()*back);
				return true;
			}
			return false;
		}
		
		protected void shieldAdjust() {
			if(shieldLeft) {
		      	if(shieldAdjustAngle<70) {
	        		shieldAdjustAngle+=5;
	        		adjustBothHandAngle();
		      	}
			}
			if(shieldRight) {
		      	if(shieldAdjustAngle>-10) {
	        		shieldAdjustAngle-=5;
		      		adjustBothHandAngle();
		      	}
			}
		}
				
		protected void swordAttack() {
			if(PathCalculator.findDist(sword, targetPoint)>=sword.getRadius()) {
				switch(attackType) {
				case 0:
					PathCalculator.straightPath(sword, targetPoint, swordSpeed);
					break;
				case 1:
					PathCalculator.circularPath(sword, targetPoint, -swordSpeed, -120);
					break;
				case 2:
					PathCalculator.circularPath(sword, targetPoint, swordSpeed, 120);
					break;
				}
			}else {
				sword.setXS(0);
				sword.setYS(0);
			}
		}
		
		protected void throwAttack() {
			throwSword=true;
			PathCalculator.straightPath(sword,targetPoint,throwSpeed);
			if(useImage) {
				sword.setImage(swordImage);
				setImage(playerNoSwordImage);
			}
		}
		
		protected void swordRetreat() {
			PathCalculator.straightPath(sword,getRightHand(),swordSpeed);
		}
		
		protected void attackAction() {
			if(!throwSword) {
				if(clicked) {
					sword.setAngle(getRightHand(),targetPoint);
					if(PathCalculator.findDist(this,targetPoint)<=attackRadius) {
						testSwordout();
						swordAttack();
					}
					else if(!swordout){
						testSwordout();
						throwAttack();
					}
					else {
						clicked=false;
					}
				}
				else {
					if(PathCalculator.findDist(sword,getRightHand())>getRightHand().getRadius()&&swordout) {
						swordRetreat();
					}else{
						if(useImage)
							setImage(playerHoldedImage);
						sword.setLocation(-10, -10);
						swordout=false;		
					}
				}
			}else {
				swordFly();
				if(sword.getXS()==0)
					pickSword();
			}
		}
		
		protected void testSwordout() {
			if(!swordout) {
				if(useImage)
					setImage(playerImage);
				sword.setLocation(getRightHand().getX(), getRightHand().getY());
				for(Point p:slashTrail) {
					p.setLocation(getRightHand().getX(), getRightHand().getY());
				}
				swordout=true;
			}
		}
		
		protected void setslashTrail(double x, double y) {
			slashTrail.removeLast();
			slashTrail.push(new Point(x,y,3));
		}
		
		protected void pickSword() {
			if(sword.getLifeSpan()>3&& PathCalculator.findDist(sword,this)<=getRadius()+sword.getRadius()) {
				swordout=false;
				throwSword=false;
				sword.setLifeSpan(0);
				sword.setLocation(-10, -10);
				if(useImage)
					sword.setImage(swordHoldedImage);
			}
		}
		
		protected void swordFly() {
			sword.addLifeSpan();
			if(sword.getLifeSpan()>=swordFlyDistance) {
				sword.setXS(0);
				sword.setYS(0);
			}
		}
		
		protected boolean isSwordout() {
			return swordout;
		}
		
	}

	private class Bot extends SwordPlayer{
		private SwordPlayer target;
		private double dot; //distance of target
		private boolean insideAttackRange;
		private int attackCD;
		private int withDrawCD;
		private int cooldown;
		private double reactionSpeed;
		
		
		private Bot(double x, double y, double size, int cd, double rs) {
			super(x, y, size);			
			cooldown=cd;
			reactionSpeed=rs;
		}
	
				
		
		protected SwordPlayer getTarget() {
			return target;
		}


		protected void findTarget() {
			if(target!=null) {
				dot=PathCalculator.findDist(this, target);
			}
			bot.forEach(b->{
				if(b!=this) {
					if(target==null) {	
						setTarget(b);
						dot=PathCalculator.findDist(this, target);
					}else {
						double dist=PathCalculator.findDist(this,b);
						if(dist<dot) {
							dot=dist;
							setTarget(b);
						}
					}
				}
			});
		}

		protected void setTarget(SwordPlayer p) {
			target=p;
			setTargetPoint(p);
		}
		
		@Override
		protected void reset() {
			throwSword=false;
			swordout=false;
			setDiameter(playerSize);
			sword.setDiameter(swordSize);
			updateHand();
		}
		
		@Override
		protected void roundAction() {
			facingAngle();
			if(target==player) {
				dot=PathCalculator.findDist(this, target);
			}
			insideAttackRange=dot<attackRadius*1.2;
			if(target.clicked) {
				shieldAdjust();
			}
			if(insideAttackRange) {
				if(!swordout) {
					if(performAttack()) {
						clicked=true;
					};
				}
			}
			if(swordout) {
				sword.move(board.getWidth(),board.getHeight());
				setslashTrail(sword.getX(),sword.getY());
			}
			if(getLeftHand().getLifeSpan()>0) {
				getLeftHand().reduceLifeSpan();
			}
			attackAction();
			adjustBothHandAngle();
			moveAction();
		}
		
		
		@Override
		protected void attackAction() {
			if(clicked) {
				sword.setAngle(getRightHand(),targetPoint);
				if(!swordout) {
					if(useImage)
						setImage(playerImage);
					sword.setLocation(getRightHand().getX(), getRightHand().getY());
					for(Point p:slashTrail) {
						p.setLocation(getRightHand().getX(), getRightHand().getY());
					}
					swordout=true;
				}
				swordAttack();
				if(withDrawCD>=50) {
					if(rand.nextBoolean()) {
						clicked=false;
					}
					else {
						setAttackType(rand.nextInt(3));
					}					
					withDrawCD=rand.nextInt(40);
				}else {
					withDrawCD++;
				}
			}
			else {
				if(PathCalculator.findDist(sword,getRightHand())>getRightHand().getRadius()&&swordout) {
					swordRetreat();
				}else{
					if(useImage)
						setImage(playerHoldedImage);
					sword.setLocation(-10, -10);
					swordout=false;		
				}
			}
		}
		
		private boolean performAttack() {
			if(attackCD==0) {
				attackCD=rand.nextInt(cooldown);
				setAttackType(rand.nextInt(3));
				return true;
			}else {
				attackCD--;
				return false;
			}
		}
		
		@Override
		protected void shieldAdjust() {
			
			double defenseAngle=PathCalculator.findAngle(target.getSword(),this);
			double anglediff = (shieldAngle - defenseAngle + 180 + 360) % 360 - 180;
			if(anglediff>25) {
				if(shieldAdjustAngle<70)
					shieldAdjustAngle+=reactionSpeed;
			}
			else if(anglediff<-25) {
				if(shieldAdjustAngle>-70)
				shieldAdjustAngle-=reactionSpeed;
			}
		}
		
		@Override
		protected void moveAction() {
			if(dot<attackRadius) {
				if(dot<(attackRadius*0.5)) {
					PathCalculator.straightPath(this,targetPoint,-movementSpeed);
				}else if(dot>(attackRadius*0.7)){		
					if(lifeSpan<=0) {
						setLifeSpan(rand.nextInt(100));
						setMoveDirection(rand.nextBoolean());
						setMoveAngle(rand.nextInt(200)+80);
					}					
					if(getMoveDirection()) {
						PathCalculator.circularPath(this, targetPoint, movementSpeed, getMoveAngle());
					}
					else {
						PathCalculator.circularPath(this, targetPoint, -movementSpeed ,-getMoveAngle());
					}
				}
			}else if(!insideAttackRange){
				PathCalculator.straightPath(this,targetPoint,movementSpeed);
			}			
			reduceLifeSpan();
			setHandPosition(getRightHand(), getAngle()-110);
			setHandPosition(getLeftHand(),shieldAngle);
			move(board.getWidth(),board.getHeight());
		}


		private void facingAngle() {
			double faceAngle=PathCalculator.findAngle(this,target);
			double anglediff = (faceAngle-getAngle()+ 180 + 360) % 360 - 180;
			if(anglediff>15) {
				setAngle(Math.toRadians(getAngle()+10));

			}
			else if(anglediff<-15) {
				setAngle(Math.toRadians(getAngle()-10));
			}
		}
	}


	private class ParticleHandler implements Runnable{
		private boolean run=true;
		private int delay=board.getTimerDelay();
		@Override
		public void run() {
			while(run) {
				bloodMove();
				sparksMove();
				arrowsMove();
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void stop() {
			run=false;
		}
		
		private void bloodMove() {
			if(!blood.isEmpty()) {
				Iterator<Point> it=blood.iterator();
				while(it.hasNext()) {
					Point p=it.next();
					if(p.getLifeSpan()<bloodTravel) {
						p.move();
						p.addLifeSpan();
					}else {
						it.remove();
						if(board.isBloodCensor()) {
							Color c=p.getColor();
							renderBackground(p,new Color(c.getRed(),c.getGreen(),c.getBlue(),130));
						}
						else
							renderBackground(p,deadBloodColor);
					}
				}
			}
		}
		
		private void sparksMove() {
			if(!sparks.isEmpty()) {
				Iterator<Point> it=sparks.iterator();
				while(it.hasNext()) {
					Point p=it.next();
					if(p.getLifeSpan()<50) {
						p.move();
						p.addLifeSpan();
					}else {
						it.remove();
					}
				}
			}
		}
		
		private void arrowsMove() {
			if(!enemyArrows.isEmpty()) {
				Iterator<Point> it=enemyArrows.iterator();
				while(it.hasNext()) {
					Point p=it.next();
					p.move();
					double dist=PathCalculator.findDist(p, player.getLeftHand());   
					if(dist<=player.getLeftHand().getDiameter()+player.getDiameter()){
						if(player.block(p,0,dist)) {
							it.remove();
							continue;
						}else if(collideDetector.checkCollide(player, p)) {
								player.addUseValue();
								addBlood(p,player);
								it.remove();
								continue;
						}
					}
					p.addLifeSpan();
					if(p.getLifeSpan()>=200) {
						it.remove();
					}
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private class StatPanel extends JDialog implements ActionListener{
		private JLabel[] lbl= {
			new JLabel("Remaining Perk: "+perk),
			new JLabel("Player Size: "+ playerSize),
			new JLabel("Sword Speed: "+ swordSpeed),
			new JLabel("Sword Size: "+ swordSize),
			new JLabel("Movement Speed: "+ movementSpeed),
			new JLabel("Add One Health Point")
		};
		private StatPanel() {
			final JButton btnPS=new JButton("+");
			final JButton btnSP=new JButton("+");
			final JButton btnSS=new JButton("+");
			final JButton btnMS=new JButton("+");
			final JButton btnTS=new JButton("+");
			btnPS.setActionCommand("1");
			btnSP.setActionCommand("2");
			btnSS.setActionCommand("3");
			btnMS.setActionCommand("4");
			btnTS.setActionCommand("5");
			btnPS.addActionListener(this);
			btnSP.addActionListener(this);
			btnSS.addActionListener(this);
			btnMS.addActionListener(this);
			btnTS.addActionListener(this);
			setLayout(new GridLayout(6,2));
			add(lbl[0]);
			add(new JLabel());
			add(lbl[1]);
			add(btnPS);
			add(lbl[2]);
			add(btnSP);
			add(lbl[3]);
			add(btnSS);
			add(lbl[4]);
			add(btnMS);
			add(lbl[5]);
			add(btnTS);
		}
		
		private void display() {
			lbl[0].setText("Remaining Perk: "+perk);
			pack();
			setTitle("Status Log");
			setModal(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(null);   
			setVisible(true);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
			case "1": 
				playerSize+=5;
				attackRadius=playerSize*4;
				player.setDiameter(playerSize);
				player.updateHand();
				perk--;
				lbl[1].setText("Player Size: "+ playerSize);
				break;
			case "2":
				swordSpeed+=0.8;
				throwSpeed=swordSpeed*2;
				swordFlyDistance=attackRadius*4/throwSpeed;
				perk--;
				lbl[2].setText("Sword Speed: "+ swordSpeed);
				break;
			case "3":
				if(swordSize<=playerSize-5) {
				swordSize+=5;
				player.getSword().setDiameter(swordSize);
				perk--;
				lbl[3].setText("Sword Size: "+ swordSize);
				}else JOptionPane.showMessageDialog(null, "Sword cannot bigger than player");
				break;
			case "4":
				perk--;
				movementSpeed+=0.3;
				lbl[4].setText("Movement Speed: "+ movementSpeed);
				break;
			case "5":
				player.reduceUseValue();
				perk--;
				break;
			}
			lbl[0].setText("Remaining Perk: "+perk);
			if(perk==0) {
				dispose();
			}
			repaint();
		}
	}	
}