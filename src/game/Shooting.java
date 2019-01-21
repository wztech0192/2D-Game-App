/*
	**********************************
	File Name: Shooting.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*2D top-down shooting game
*/

package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import object.Point;
import object.Board;
import tool.Animator;
import tool.ImageEditor;
import tool.PathCalculator;



 	class Shooting extends GameSetter{
	
	private LinkedList<Point> bossBullet=new LinkedList<Point>();
	private LinkedList<Point> bullet=new LinkedList<Point>();
	private LinkedList<Point> blood=new LinkedList<Point>();
	private BufferedImage background;

	private Animator bulletAnimator;
	private Animator playerAnimator;
	private Animator bossAnimator;
	private Animator[] bossBulletAnimator;	
	private Animator[] monsterAnimator;
	
	
	private boolean useImage;
	private int playerSize;
	
	private int defaultAmmoCap, ammo_capicity, ammo;
	private double defaultMovementSpeed=2, movementSpeed;
	private int defaultBulletSize, bulletSize;
	private double defaultBulletSpeed,bulletSpeed;
	private int defaultFireRate, fireRate;
	private int startingLevel;
	
	private int startingEnemyNumber;
	private int bloodTravel;
	
	private int perk;
	private int kill;
	
	private StatPanel statPane;

	
	private int enemyNumber;
	private int wait;
	private int waitTime;
	private int fireCD;
	
	private boolean clicked=false;
	private Point mouse=new Point(0,0);
	
	private boolean boss=false;
	//private int bossHP;

	private final Color bloodColor=new Color(0.5f,0f,0f,.8f);
	private final Color deadBloodColor=new Color(0.5f,0f,0f,.5f);
	private final Color bossBloodColor=new Color(0f,0.3f,0f,.6f);
	private final Color bossDeadBloodColor=new Color(0f,0.3f,0f,.3f);

	public Shooting(Board board) {
		super(board);
	}
	

	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
    	boolean yy=rand.nextBoolean();
    	boolean xx=rand.nextBoolean();
    	Point monster;
    	if(xx && yy) {
    		monster= new Point(width,rand.nextInt(height),size);
    	}
    	else if(!xx && !yy) {
    		monster= new Point(rand.nextInt(width),height,size);
    	}
    	else if(yy) {
    		monster= new Point(rand.nextInt(width),0,size);
    	}
    	else
    		monster= new Point(0,rand.nextInt(height),size);
    			
		if(boss) {
			monster.setUseValue(getLevel()*15);  //boss hp
			monster.setDefaultLifeSpan(100);       
			monster.setDiameter(player.getDiameter()*5);
			if(useImage) {
				monster.setAnimator(bossAnimator);
				bossAnimator.animateStart();
				bossAnimator.setAnimateLoop(true);
				for(Animator a:bossBulletAnimator) {
					a.animateStart();
					a.setAnimateLoop(true);
				}
			}
		}else {
			if(useImage) {
				monster.setAnimator(monsterAnimator[rand.nextInt(2)]);
			}
			monster.setDefaultLifeSpan(rand.nextInt(91));
		}
		
    	return monster;
	}
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/shooting/st_config"));
			if(br.readLine().split("\\|")[1].equals("T")) {
				useImage=true;
			}else useImage=false;
			String[] input=br.readLine().split("\\|");
			playerSize=(int)Math.ceil(Double.parseDouble(input[0]));
			defaultAmmoCap=(int)Math.ceil(Double.parseDouble(input[1]));
			defaultBulletSize=(int)Math.ceil(Double.parseDouble(input[2]));
			defaultBulletSpeed=(int)Math.ceil(Double.parseDouble(input[3]));
			defaultMovementSpeed=(int)Math.ceil(Double.parseDouble(input[4]));
			startingEnemyNumber=(int)Math.ceil(Double.parseDouble(input[5]));
			bloodTravel=(int)Math.ceil(Double.parseDouble(input[6]));
			waitTime=(int)Math.ceil(Double.parseDouble(input[7]));
			defaultFireRate=(int)Math.ceil(Double.parseDouble(input[8]));
			startingLevel=(int)Math.ceil(Double.parseDouble(input[9]));
			br.close();
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/shooting/st_config\" not found");
		}
	}
	
	
	@Override
	public void gameStart() {
		loadSetting();
		player=new Player(board.getWidth()/2,board.getHeight()/2,playerSize);
		if(useImage) {
			loadImage();
			player.setAnimator(playerAnimator);
		}
		gameReset();
		statPane=new StatPanel();
	};
	
	private void loadImage() {
		BufferedImage bullet=ImageEditor.loadImage("/image/game_image/shooting_image/bullet.png");
		bulletAnimator=new Animator(ImageEditor.getSprite(bullet, 0,0, 8,1, 100, 100),0, 100);
		BufferedImage player=ImageEditor.loadImage("/image/game_image/shooting_image/player.png");
		playerAnimator=new Animator(ImageEditor.getSprite(player, 0,0, 4,1, 100, 100),0, 100);
		BufferedImage boss=ImageEditor.loadImage("/image/game_image/shooting_image/boss.png");
		bossAnimator=new Animator(ImageEditor.getSprite(boss, 0,0, 5,1, 200, 200),0, 100);
		BufferedImage bossbullet[]= {	
				ImageEditor.loadImage("/image/game_image/shooting_image/bossbullet1.png"),
				ImageEditor.loadImage("/image/game_image/shooting_image/bossbullet2.png")
		};
		bossBulletAnimator=new Animator[2];
		bossBulletAnimator[0]=new Animator(ImageEditor.getSprite(bossbullet[0], 0,0, 33,1, 10, 10),0, 50);
		bossBulletAnimator[1]=new Animator(ImageEditor.getSprite(bossbullet[1], 0,0, 18,1, 10, 10),0, 50);
		
		BufferedImage monster[]= {
				ImageEditor.loadImage("/image/game_image/shooting_image/monster1.png"),
				ImageEditor.loadImage("/image/game_image/shooting_image/monster2.png")
		};
		monsterAnimator=new Animator[2];
		monsterAnimator[0]=new Animator(ImageEditor.getSprite(monster[0], 0,0, 8,1, 100, 100),0, 100);
		monsterAnimator[1]=new Animator(ImageEditor.getSprite(monster[1], 0,0, 3,1, 100, 100),0, 100);
		
		playerAnimator.animateStart();
		playerAnimator.setAnimateLoop(true);
		bulletAnimator.animateStart();
		bulletAnimator.setAnimateLoop(true);
		for(Animator m:monsterAnimator) {
			m.animateStart();
			m.setAnimateLoop(true);
		}
	}
	
	@Override
	public void gameReset() {
		resetScore();
		level=startingLevel;
		background = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
		perk=0;
		wait=waitTime;
		player.stopMove();
		bulletSpeed=defaultBulletSpeed;
		movementSpeed=defaultMovementSpeed;
		bulletSize=defaultBulletSize;
		ammo_capicity=defaultAmmoCap;
		ammo=defaultAmmoCap;
		fireRate=defaultFireRate;
		kill=0;
		if(!bullet.isEmpty()) {
			bullet.clear();
		}
		if(!blood.isEmpty()) {
			blood.clear();
		}
		if(boss) {
			bossAnimator.animateStop();
			for(Animator b:bossBulletAnimator) {
				b.animateStop();
			}
			if(!bossBullet.isEmpty()) {
				bossBullet.clear();
			}
		}
		board.clearPoint();
		enemyNumber=startingEnemyNumber*getLevel();
	};
	
	@Override
	public void gameOver() {
		int n=JOptionPane.showConfirmDialog(null,"Game Over!!\nYour Score: "+score+"\nYour Level: "+getLevel()+"\n\nDo You Want To Restart?","Restart?",JOptionPane.YES_NO_OPTION);
		if(n==JOptionPane.YES_OPTION) {
			gameReset();
		}else board.refresh();
	}
	
	@Override
	public void gameClear() {
		playerAnimator.animateStop();
		bulletAnimator.animateStop();
		for(Animator m:monsterAnimator) {
			m.animateStop();
		}
		if(boss) {
			bossAnimator.animateStop();
			for(Animator b:bossBulletAnimator) {
				b.animateStop();
			}
		}
		
		if(!bossBullet.isEmpty()) {
			bossBullet.clear();
		}
		if(!blood.isEmpty()) {
			blood.clear();
		}
		if(!bullet.isEmpty()) {
			bullet.clear();
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
        switch(e.getKeyCode()) {
        case KeyEvent.VK_W:
        	getPlayer().setMoveUp(true);
        	break;
        case KeyEvent.VK_S:
        	getPlayer().setMoveDown(true);
        	break;
        case KeyEvent.VK_A:
        	getPlayer().setMoveLeft(true);
        	break;
        case KeyEvent.VK_D:
        	getPlayer().setMoveRight(true);
        	break;
        case KeyEvent.VK_P:
        	if(perk>0) {
        		board.pause(true);
        		statPane.display();
        		board.pause(false);
        	}
        }
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		 switch (e.getKeyCode()) {
	        case KeyEvent.VK_W:
	        	getPlayer().setMoveUp(false);
	        	break;
	        case KeyEvent.VK_S:
	        	getPlayer().setMoveDown(false);
	        	break;
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
		if(player.getMoveUp()) {
			double newY=player.getY()-movementSpeed;
			if(newY>0.0)
				player.setY(newY);
		}
		if(player.getMoveDown()) {
			double newY=player.getY()+movementSpeed;
			if(newY<board.getHeight())
				player.setY(newY);
		}
		if(player.getMoveLeft()) {
			double newX=player.getX()-movementSpeed;
			if(newX>0.0)
				player.setX(newX);
			
		}if(player.getMoveRight()){
			double newX=player.getX()+movementSpeed;
			if(newX<board.getWidth())
				player.setX(newX);
		}
	}
	

	@Override
	public void mouseClicked(MouseEvent e) {
		clicked=true;
		fireCD=0;
		mouse.setLocation(e.getX(), e.getY());
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		clicked=false;
	}
	
	@Override
	public void mouseMove(MouseEvent e) {
		mouse.setLocation(e.getX(), e.getY());
		if(useImage){
			player.setAngle(mouse);
		}
	}
	
	@Override
	public void roundAction() {
		if(!board.isFocusOwner()) {
			board.requestFocus();
		}
		playerMove();
		fireBullet();
		ArrayList<Point> points=board.getPoints();
		bloodMove();
		bulletAction(points);
		if(!waiting()) {
			spawnEnemy();
			if(collideDetector.testCollide(player,points)){
				gameOver();
				return;
			}
			else {
				if(boss) {
					Point b=points.get(0);
					bossMove(b);
					bossBulletMove();
				}else {
					monsterMove(points);
					if(enemyNumber<=0 && points.isEmpty()) {
						levelUp();
					}
				}
			}
		}
	};
	
	
	@Override
	public void levelUp() {
		wait=waitTime;
		level++;
		if(level%10==0) {
			boss=true;
			enemyNumber=1;
		}else {
			enemyNumber=startingEnemyNumber*getLevel();
		}
	};
	
	@Override
	public void drawBack(Graphics2D g2d) {
		g2d.setColor(Color.cyan);
		g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(), null);
		g2d.drawString("**Shooting**", (board.getWidth()/2)-40, 13);
		g2d.drawString("Your Score: "+getScore(), board.getWidth()-100, 13);
		g2d.drawString("Game Level: "+getLevel(), board.getWidth()-100, 26);
		g2d.drawString("Your  Ammo: "+ammo+"/"+ammo_capicity, board.getWidth()-100, 39);
		g2d.drawString("# oF Kills: "+kill, board.getWidth()-100, 52);
		if(perk>0) {
			g2d.drawString("Press \"P\" to Upgrade Your Stats", (board.getWidth()/2)-80, 26);
		}
		if(wait>0) {
			if(boss) {
				g2d.drawString("BOSS is COMING IN "+ wait/40, (board.getWidth()/2)-70, 39);
				g2d.drawString("BOSS is COMING IN "+ wait/40, (board.getWidth()/2)-70, 52);
				g2d.drawString("BOSS is COMING IN "+ wait/40, (board.getWidth()/2)-70, 65);
				g2d.drawString("BOSS is COMING IN "+ wait/40, (board.getWidth()/2)-70, 78);
			}
			else g2d.drawString("New Wave is COMING IN "+ wait/40, (board.getWidth()/2)-70, 39);
		}
		if(!hasAmmo()) {
			g2d.drawString("No Ammo!!!", (int)(player.getX()-player.getRadius())-5,(int)(player.getY()-player.getRadius())-5);
		}	
		g2d.setColor(Color.blue);
		player.drawWithImageBigger(g2d);
		bullet.forEach(p->p.drawWithImageBigger(g2d));
	};
	
	
	@Override
	public void drawFront(Graphics2D g2d) {
		if(boss) {
			bossBullet.forEach(p->p.drawWithImageBigger(g2d));
		}
		blood.forEach(p->p.drawWithColor(g2d));
	}
	
	
	private void renderBackground(Point p, Color c) {
		if(board.getWidth()!=background.getWidth() || board.getHeight()!=background.getHeight()) {
			BufferedImage newBackground = new BufferedImage(board.getWidth(), board.getHeight(),BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d=newBackground.createGraphics();		
			g2d.drawImage(background, 0, 0, background.getWidth(), background.getHeight(),null);
			background=newBackground;
		}
		Graphics2D g2d=background.createGraphics();		
		if(c==deadBloodColor) {
			if(useImage) {
				if(rand.nextBoolean()) {
					p.drawWithImageBigger(g2d);
					return;
				}
			}
			if(board.isBloodCensor()) {
				c=new Color(rand.nextFloat(),rand.nextFloat(),rand.nextFloat(),0.5f);
			}
		}	
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
	
	private void bossBulletMove() {
		if(!bossBullet.isEmpty()) {
			Iterator<Point> it=bossBullet.iterator();
			while(it.hasNext()) {
				Point p=(Point)it.next();
				p.setAngle(player);
				p.move();
				if(collideDetector.checkCollide(player,p)) {
					gameOver();
					return;
				};
				if(p.getLifeSpan()>150) {
					it.remove();
					continue;
				}
				p.addLifeSpan();
			}
		}
	}

	private void bossAttack(Point p) {
		Point b=new Point(p.getX(),p.getY(),p.getDiameter()/10);
		PathCalculator.straightPath(b, player, 5);
		if(useImage) {
			b.setAnimator(bossBulletAnimator[0]);
		}
		bossBullet.add(b);
	}
	
	private void bossSuperAttack(Point p) {
		Point b=new Point(p.getX(),p.getY(),p.getDiameter()/10);
		if(useImage) {
			b.setAnimator(bossBulletAnimator[1]);
		}
		bossBullet.add(b);
		int angle=180-rand.nextInt(360);
		int speed=(angle>0)?5:-5;
		PathCalculator.circularPath(b, player, speed,angle);
	}
	
	private void bossMove(Point p) {
		p.setAngle(player);
		double speed=4-(p.getUseValue()/10);
		if(speed<2) {
			speed=2;
		}
		if(p.getLifeSpan()<=0) {
			p.setLifeSpan(rand.nextInt(50));
			p.setMoveDirection(rand.nextBoolean());
			p.setMoveAngle(rand.nextInt(100)+140);
		}
		if(p.getMoveDirection()) {
			PathCalculator.circularPath(p, player, speed, p.getMoveAngle());
		}
		else {
			PathCalculator.circularPath(p, player, -speed ,-p.getMoveAngle());
		}
		if(p.getLifeSpan()%(20)==0) {
			bossAttack(p);
			if(p.getUseValue()<=25) {
				for(int i=0;i<10;++i) {
					bossSuperAttack(p);
				}
			}
		}
		p.reduceLifeSpan();
	}
		
	private void fireBullet() {
		if(clicked) {
			if(hasAmmo() && fireCD<=0) {
				Point bullet= new Point(player.getX(),player.getY(),bulletSize);
				PathCalculator.straightPath(bullet,mouse,bulletSpeed);
				if(useImage) {
					bullet.setAnimator(bulletAnimator);
					bullet.setAngle(mouse);
				}
				shoots(bullet);
				fireCD=fireRate;
			}else fireCD--;
		}
	}
	
	
	private void spawnEnemy() {
		if(enemyNumber>0) {
			if(rand.nextInt(30)%3==0 || boss) {
				board.addPoint();
				enemyNumber--;
			}
		}
	}
	

	private void monsterMove(ArrayList<Point> points) {
		for(int i=0;i<points.size();++i){
			Point p=points.get(i);
			if(useImage) {
				p.setAngle(player);
			}
			double speed=Math.sqrt(getLevel())/3;
			if(p.getLifeSpan()<=0) {
				p.setLifeSpan(rand.nextInt(50));
				p.setMoveDirection(rand.nextBoolean());
				p.setMoveAngle(rand.nextInt(100)+10);
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
	
	
	private void shoots(Point b) {
		ammo--;
		bullet.add(b);
	}
	
	
	private void increaseAmmo() {
		ammo_capicity+=1;
		ammo+=1;
	}	

	private boolean hasAmmo() {
		return ammo!=0;
	}	
	
	private void bulletAction(ArrayList<Point> points) {
		if(!bullet.isEmpty()) {
			Iterator<Point> it=bullet.iterator();
			while(it.hasNext()) {
				Point b=(Point)it.next();
				b.move();
				Point p=bulletHitTarget(points,b);
				if(p!=null) {
					addBlood(b,p);		
					addScore(250);
					if(boss) {
						p.reduceUseValue();
						if(p.getUseValue()<=0){
							boss=false;
							bossAnimator.animateStop();
							for(Animator a:bossBulletAnimator) {
								a.animateStop();
							}
							bossBullet.clear();
							renderBackground(p,bossDeadBloodColor);
							levelUp();
						}
					}else {
						kill++;
						renderBackground(p,deadBloodColor);
						if(kill%8==0) {
							perk++;
						}
					}
					if(p.getDiameter()>b.getDiameter()) {
						ammo++;
						it.remove();
					}
					else {
						b.setDiameter(b.getDiameter()/2);
					}
					continue;
				}
				b.addLifeSpan();   //bullet disappear after amount of time
				if(b.getLifeSpan()>75) {
					ammo++;
					it.remove();
				}
			}
		}
	}
	
	private Point bulletHitTarget(ArrayList<Point> points, Point b) {
		for(Point p:points) {
			double distance=(b.getRadius()+p.getRadius())-PathCalculator.findDist(p,b);
			if(distance>=0) {
				if(!boss)
				points.remove(p);
				return p;
			}
		}
		return null;
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
					else {
						if(boss) {
							renderBackground(p,bossBloodColor);
						}else renderBackground(p,bloodColor);	
					}
				}
			}
		}
	}
	
	private void addBlood(Point b, Point p) {
		double size=p.getRadius();
		if(size>20) {
			size=20;
		}
		for(int i=0; i<size;++i) {
			double sx=p.getX()-p.getRadius();
			double mx=p.getX()+p.getRadius();
			double sy=p.getY()-p.getRadius();
			double my=p.getY()+p.getRadius();
			Point bld=new Point(sx+(mx-sx)*rand.nextDouble(),sy+(my-sy)*rand.nextDouble(),size*rand.nextDouble(),b.getXS()*rand.nextDouble()*0.8,b.getYS()*rand.nextDouble()*0.8);
			if(board.isBloodCensor()) {
				bld.setColor(new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(),0.8f));
			}
			else {
				if(boss) {
					bld.setColor(bossBloodColor);
				}
				else bld.setColor(bloodColor);
			}
			blood.add(bld);
		}
	}
	
	private class StatPanel extends JDialog implements ActionListener{
		private JLabel[] lbl= {
			new JLabel("Remaining Perk: "+perk),
			new JLabel("Bullet Speed: "+ bulletSpeed),
			new JLabel("Bullet Size: "+ bulletSize),
			new JLabel("Movement Speed: "+ movementSpeed),
			new JLabel("Ammo Capicity: "+ ammo_capicity),
			new JLabel("Fire Speed: "+fireRate) 
		};
		private StatPanel() {
			final JButton btnBP=new JButton("+");
			final JButton btnMP=new JButton("+");
			final JButton btnBS=new JButton("+");
			final JButton btnAS=new JButton("+");
			final JButton btnFR=new JButton("+");
			btnBP.setActionCommand("1");
			btnBS.setActionCommand("2");
			btnMP.setActionCommand("3");
			btnAS.setActionCommand("4");
			btnFR.setActionCommand("5");
			btnBP.addActionListener(this);
			btnMP.addActionListener(this);
			btnAS.addActionListener(this);
			btnBS.addActionListener(this);
			btnFR.addActionListener(this);
			setLayout(new GridLayout(6,2));
			add(lbl[0]);
			add(new JLabel());
			add(lbl[1]);
			add(btnBP);
			add(lbl[2]);
			add(btnBS);
			add(lbl[3]);
			add(btnMP);
			add(lbl[4]);
			add(btnAS);
			add(lbl[5]);
			add(btnFR);
		}
		
		private void display() {
			lbl[0].setText("Remaining Perk: "+perk);
			lbl[1].setText("Bullet Speed: "+ bulletSpeed);
			lbl[2].setText("Bullet Size: "+ bulletSize);
			lbl[3].setText("Movement Speed: "+ movementSpeed);
			lbl[4].setText("Ammo Capicity: "+ ammo_capicity);
			lbl[5].setText("Fire Speed: "+fireRate);
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
				bulletSpeed++;
				perk--;
				lbl[1].setText("Bullet Speed: "+ bulletSpeed);
				break;
			case "2":
				bulletSize+=5;
				perk--;
				lbl[2].setText("Bullet Size: "+ bulletSize);
				break;
			case "3":
				perk--;
				movementSpeed+=0.5;
				lbl[3].setText("Movement Speed: "+ movementSpeed);
				break;
			case "4":
				increaseAmmo();
				lbl[4].setText("Ammo Capicity: "+ ammo_capicity);
				perk--;
				break;
			case "5":
				if(fireRate>1) {
					fireRate--;
					lbl[5].setText("Fire Speed: "+ fireRate);
					perk--;
				}else JOptionPane.showMessageDialog(null, "Maxed");
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
