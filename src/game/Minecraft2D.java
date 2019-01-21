/*
	**********************************
	File Name: Minecraft2D.java
	Package: game
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Open-World 2D block game with infinity size of map
*/

package game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JOptionPane;

import object.Board;
import object.Point;
import tool.Animator;
import tool.FolderEditor;
import tool.ImageEditor;

 enum Tool{
	Empty_Hand,Sword,Pickaxe,Wall_Breaker,Dirt,Rock,Stone,Wood,Stone_Wall,Wood_Wall,Window_Wall,Leaf_Wall,Touch,Door1,Door2,Door3,Door4;
}

class Minecraft2D extends GameSetter{

	private LinkedList<Point> dust=new LinkedList<Point>();
	private LinkedList<Point> blood=new LinkedList<Point>();

	private HashMap<Integer,HashMap<Integer,Integer>> left_wall;
	private HashMap<Integer,HashMap<Integer,Integer>> right_wall;
	private HashMap<Integer,HashMap<Integer,Point>> map;
	private ConcurrentLinkedQueue<Monster> monster=new ConcurrentLinkedQueue<Monster>();
	
	private BufferedImage blockMap;
	private BufferedImage sub_blockMap;
	
	private int zoneSize;
	private int blockSize;
	private int movementSpeed;
	private int monsterSpeed=1;
	private int playerSize;
	
	private boolean moved=true;
	private int xCenter,yCenter;
	private int blockMap_xCenter, blockMap_yCenter;
	
	
	private int area_zone=0;
	private Thread save_map;
	private Thread load_map;
	private boolean shifting;
	private final Color selectColor=new Color(80,50,50,50);
	private final Color transparentColor=new Color(0,0,0,0);
	private final Color bloodColor=new Color(0f,0.3f,0f,.6f);
	private final String fileURL="config/game_config/mc2d/map/zone_";
	
	private boolean useImage=false;
	
	private BufferedImage[] background;
	private BufferedImage[] blockImage;
	private BufferedImage[] wallImage;
	private Animator playerAnimator, armAnimator;
	private Animator[] monsterAnimator;
	
	private final Point mouse=new Point(0,0);
	private boolean mouseInLeft;
	private boolean lookDirection;
	private boolean clicked;
	private Tool selected_tool=Tool.Empty_Hand;
	private final Tool[] tools= {
			Tool.Empty_Hand,Tool.Sword,Tool.Pickaxe,Tool.Wall_Breaker,
			Tool.Dirt,Tool.Rock,Tool.Stone,Tool.Wood,Tool.Stone_Wall,Tool.Wood_Wall,Tool.Window_Wall,
			Tool.Leaf_Wall,Tool.Touch,Tool.Door1,Tool.Door2,Tool.Door3,Tool.Door4
	};
	
	private MonsterHandler monsterRun;
	
	private final int blockHP=1000;
	private int digPower;
	
	
	public Minecraft2D(Board board) {
		super(board);
	}
	
	
	@Override
	public void loadSetting() {
		try{
			BufferedReader br= new BufferedReader(new FileReader("config/game_config/mc2d/mc2d_config"));
			String[] booleanInput=br.readLine().split("\\|");
			if(booleanInput[1].equals("T"))
				useImage=true;
			else useImage=false;
			String[] input=br.readLine().split("\\|");
			zoneSize=(int)Math.ceil(Double.parseDouble(input[0]));
			int blockRatio=(int)Math.ceil(Double.parseDouble(input[1]));
			blockSize=zoneSize/blockRatio;
			playerSize=(int)Math.ceil(Double.parseDouble(input[2]));
			movementSpeed=(int)Math.ceil(Double.parseDouble(input[3]));
			digPower=(int)Math.ceil(Double.parseDouble(input[4]));
			if(zoneSize%2!=0 || (blockRatio%2!=0)) {
				JOptionPane.showMessageDialog(null,"All Config Input Must Be Even Number");
				board.refresh();
			}
			br.close();			
		}catch(IOException|NullPointerException e){
			JOptionPane.showMessageDialog(null, "\"config/game_config/mc2d/mc2d_config\" not found");
		}
	}
	
	
	@Override
	public void gameStart() {
		board.clearPoint();
		loadSetting();
		loadImage();
		map=new HashMap<Integer,HashMap<Integer,Point>>();
		left_wall=new HashMap<Integer,HashMap<Integer,Integer>>();
		right_wall=new HashMap<Integer,HashMap<Integer,Integer>>();
		blockMap= new BufferedImage(zoneSize, zoneSize,BufferedImage.TYPE_INT_ARGB);
		blockMap_xCenter=blockMap.getWidth()/2;
		blockMap_yCenter=blockMap.getHeight()/2;
		xCenter=board.getWidth()/2;
		yCenter=board.getHeight()/2;
		check_save_map();
		player=new Player((blockMap_xCenter),(blockMap_yCenter)-(playerSize*4),playerSize,playerSize*2);
		monsterRun=new MonsterHandler();
		Thread m=new Thread(monsterRun);
		m.start();
		updateSubBackground();
	}
	
	
	@Override
	public void gameReset() {
		while(shifting) {//wait until shifting is down
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		map.clear();
		left_wall.clear();
		right_wall.clear();
		monster.clear();
		clearMap(0,0,zoneSize,zoneSize);
		check_save_map();
		player.setLocation((blockMap_xCenter),(blockMap_yCenter)-(playerSize*4));
		updateSubBackground();
	}
	
	
	private void loadImage() {
		blockImage=ImageEditor.getSprite(ImageEditor.loadImage("/image/game_image/mc2d_image/blockSprite.png"),0,0,23, 20, 16, 16);
		wallImage=ImageEditor.getSprite(ImageEditor.loadImage("/image/game_image/mc2d_image/wallSprite.png"),0,0,4, 1, 16, 16);
		background=new BufferedImage[2];
		background[0]=ImageEditor.loadImage("/image/game_image/mc2d_image/background.jpg");
		background[1]=ImageEditor.loadImage("/image/game_image/mc2d_image/underground.png");
		BufferedImage monsterImage=ImageEditor.loadImage("/image/game_image/mc2d_image/monsterSprite.png");
		monsterAnimator=new Animator[2];
		monsterAnimator[0]=new Animator(ImageEditor.getSprite(monsterImage,0,0,4, 1, 25, 47),0,150);
		monsterAnimator[1]=new Animator(ImageEditor.getSprite(monsterImage,0,1,4, 2, 25, 47),0,150);
		monsterAnimator[0].setAnimateLoop(true);
		monsterAnimator[1].setAnimateLoop(true);
		playerAnimator=new Animator(ImageEditor.getSprite(ImageEditor.loadImage("/image/game_image/mc2d_image/playerSprite.png"),0,0,5, 1, 27, 53),0,150);
		playerAnimator.animateStart();
		armAnimator=new Animator(ImageEditor.getSprite(ImageEditor.loadImage("/image/game_image/mc2d_image/playerArmSprite.png"),0,0,7, 8, 55, 52),0,150);
		armAnimator.animateStart();	
	}
	
	
	private void check_save_map() {
		File file1 = new File(fileURL+0);
		File file2 = new File(fileURL+-1);
		if(file1.exists() && !file1.isDirectory() && file2.exists() && !file2.isDirectory()) {
			
			try {
				loadMap(file1,0,blockMap_xCenter,true);
				loadMap(file2,blockMap_xCenter,blockMap.getWidth(),false);
			}
			catch(Exception e) {
				FolderEditor.deleteDirectory("config/game_config/mc2d/map");
				generateBlock(0,blockMap.getWidth());
			}
		
		}
		else {
			generateBlock(0,blockMap.getWidth());
		}
	}
	

	@Override
	public void gameOver() {	
	}
	
	
	@Override
	public void gameClear() {
		while(shifting) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		monsterRun.stop();
		playerAnimator.animateStop();
		save_map=new Thread(new Runnable() {
			@Override
			public void run() {
				saveMap(area_zone-1,blockMap_xCenter,blockMap.getWidth(),right_wall);
				saveMap(area_zone,0,blockMap_xCenter,left_wall);
			}
			
		});
		save_map.start();
	}
	
	
	@Override
	public void levelUp() {
	}
	
	
	@Override
	public void playerMove() {
		playerMove_LeftRight();
		playerMove_UpDown();
		
	}
	
	
	private void playerMove_LeftRight() {
		if(player.getMoveLeft() && !player.getMoveRight()) {
			if(player.getX()>blockMap.getWidth()*0.1 || shifting) {
				double newX=player.getX()-movementSpeed;
				if(!leftORright_barrier(player,(int)newX)) {
		        	playerAnimator.animateSetting(3, 4, true);
		        	armAdjust();
					moved=true;
					player.setX(newX);
					adjustParticle(true,movementSpeed);
				}
				else {
					if(player.getX()%blockSize!=0) {
						player.setX(getBlockValue((int)player.getX()));
					}
				}
			}
			else {
				shiftAction(area_zone-1,++area_zone,blockMap_xCenter,blockMap.getWidth(),right_wall,false);
			}
		}
		
		if(player.getMoveRight() && !player.getMoveLeft()){	
			if(player.getX()<blockMap.getWidth()*0.9 || shifting) {
			double newX=player.getX()+movementSpeed;
				if(!leftORright_barrier(player,(int)(newX+player.getWidth()))) {
		        	playerAnimator.animateSetting(1, 2, true);
		          	armAdjust();
					moved=true;
					player.setX(newX);
					adjustParticle(true,-movementSpeed);
				}
				else {
					if((player.getX()+player.getWidth())%blockSize!=0) {
						player.setX(getBlockValue((int)player.getX()+player.getWidth()-1)+blockSize-player.getWidth());
					}
				}
			}
			else {
				shiftAction(area_zone,--area_zone-1,0,blockMap_xCenter,left_wall,true);
			}
		}
	}
	

	private boolean leftORright_barrier(Player p,int newX) {
		int x=getBlockValue(newX);
		if(map.containsKey(x)){
			HashMap<Integer,Point> mapX=map.get(x);
			int minY=getBlockValue((int)(p.getY()+1));
			int maxY=getBlockValue((int)(p.getY()+p.getHeight()-1));
			if(minY==maxY) {
				return mapX.containsKey(minY);
			}
			else {
				while(minY<=maxY) {
					if(mapX.containsKey(minY)) {
						return true;
					}
					minY+=blockSize;
				}
			}
		}
		return false;
	}


	private void playerMove_UpDown() {
		if(player.getMoveUp() && player.getUseValue()==0) { 
			player.setUseValue(20);  //use value == jump height value
		}
		//jump when jump value >1
		if(player.getUseValue()>1) {  
			if(player.getY()>blockMap.getHeight()*0.1) {
				double newY=player.getY()-movementSpeed;
				if(!upperORlower_barrier(player,(int)newY)) {
					moved=true;
					player.setY(newY);
					adjustParticle(false,movementSpeed);
					player.reduceUseValue(); 
				}
				else player.setUseValue(1);  //stop jumping
			}
		} 	//test fallen when jump value<1 and player moved
		else if(moved) {
			if(player.getY()<blockMap.getHeight()*0.9) {
				double newMovement=movementSpeed*1.5;
				double newY=player.getY()+newMovement;
				if(!upperORlower_barrier(player,(int)(newY+player.getHeight()))) {
					player.setY(newY);
					adjustParticle(false,(int)-newMovement);
				}
				else {
					//make sure player wont stuck into the block
					if((player.getY()+player.getHeight())%blockSize!=0) {
						player.setY(getBlockValue((int)player.getY()+player.getHeight()-1)+blockSize-player.getHeight());
						updateSubBackground();
					}
					//stop falling detecting when player stop move and stop falling
					if(!player.getMoveLeft() && !player.getMoveRight()) {
						moved=false;
			        	playerAnimator.defaultAnimate();
			        	armAnimator.defaultAnimate();
					}
					player.setUseValue(0); //reset jump height
				}
			}
			else {
				player.setUseValue(0);
			}
		}
	}
	
	
	private boolean upperORlower_barrier(Player p,int newY) {
		int minX=getBlockValue((int)(p.getX()+1));
		int maxX=getBlockValue((int)(p.getX()+p.getWidth()-1));
		int y=getBlockValue(newY);
		if(minX==maxX) {
			try {
				if(map.get(minX).containsKey(y)) {
					return true;
				}
			}
			catch(NullPointerException ex) {
			}
		}else {
			while(minX<=maxX) {
				try {
					if(map.get(minX).containsKey(y)) {
						return true;
					};
				}
				catch(NullPointerException ex) {
				}
				finally {
					minX+=blockSize;
				}
			}
		}
		return false;
	}
	
	
	private void armAdjust() {
	   	if(!clicked) {
			if(mouseInLeft) {
	       		switch(selected_tool) {
	       		case Empty_Hand:
	    			armAnimator.animateSetting(11, 12, true);
	    			armAnimator.setDefault(13);
	       			break;
	       		case Sword:
	    			armAnimator.animateSetting(53, 54, true);
	    			armAnimator.setDefault(55);
	       			break;
	       		case Wall_Breaker:
	       		case Pickaxe:
	    			armAnimator.animateSetting(25, 26, true);
	    			armAnimator.setDefault(27);
	       			break;
	       		default:
	    			armAnimator.animateSetting(39, 40, true);
	    			armAnimator.setDefault(41);
	       			break;
	       		}
			}
			else {
	       		switch(selected_tool) {
	       		case Empty_Hand:
	    			armAnimator.animateSetting(1, 2, true);
	    			armAnimator.setDefault(0);
	       			break;
	       		case Sword:
	    			armAnimator.animateSetting(43, 44, true);
	    			armAnimator.setDefault(42);
	       			break;
	       		case Wall_Breaker:
	       		case Pickaxe:
	    			armAnimator.animateSetting(15, 16, true);
	    			armAnimator.setDefault(14);
	       			break;
	       		default :
	    			armAnimator.animateSetting(29, 30, true);
	    			armAnimator.setDefault(28);
	       			break;
	       		}
			}
		}
	}

	
	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
			case KeyEvent.VK_SPACE:
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
				int confirm = JOptionPane.showConfirmDialog(null,"Do You Want To Reset the Map?" ,"CONFIRM" ,JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION){
					FolderEditor.deleteDirectory("config/game_config/mc2d/map");
					gameReset();
					moved=true;
				}
	        	break;
	        case KeyEvent.VK_Q:
	        	toolIndexAdjust(-1);
	        	break;
	        case KeyEvent.VK_E:
	        	toolIndexAdjust(1);
	        	break;
	        case KeyEvent.VK_1:
	        	selected_tool=Tool.Empty_Hand;
	        	armAnimator.setDefault((mouseInLeft)?13:0);
	        	break;
	        case KeyEvent.VK_2:
	        	selected_tool=Tool.Sword;
	        	armAnimator.setDefault((mouseInLeft)?55:42);
	        	break;
	        case KeyEvent.VK_3:
	        	selected_tool=Tool.Pickaxe;
	        	armAnimator.setDefault((mouseInLeft)?27:14);
	        	break;
	        case KeyEvent.VK_4:
	        	armAnimator.setDefault((mouseInLeft)?27:14);
	        	selected_tool=Tool.Wall_Breaker;
	        	break;
	        case KeyEvent.VK_5:
	        	selected_tool=Tool.Dirt;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
	        case KeyEvent.VK_6:
	        	selected_tool=Tool.Rock;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
	        case KeyEvent.VK_7:
	        	selected_tool=Tool.Stone;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
	        case KeyEvent.VK_8:
	        	selected_tool=Tool.Wood;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
	        case KeyEvent.VK_9:
	        	selected_tool=Tool.Stone_Wall;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
	        case KeyEvent.VK_0:
	        	selected_tool=Tool.Wood_Wall;
	        	armAnimator.setDefault((mouseInLeft)?41:28);
	        	break;
		}
	}
	
	
	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
		  	case KeyEvent.VK_SPACE:
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
		}
	}
	
	
	@Override
	public void mouseWheel(MouseWheelEvent e) {
		if(e.getWheelRotation() < 0) {
			toolIndexAdjust(-1);
		}
		else {
			toolIndexAdjust(1);
		}
	}
	private void toolIndexAdjust(int n) {
		int index=selected_tool.ordinal()+n;
		if(index<0) {
			index=tools.length-1;
		}
		else if(index>=tools.length){
			index=0;
		}
		selected_tool=tools[index];
		if(index==0) {
			armAnimator.setDefault((mouseInLeft)?13:0);
		}
		else if(index<=2) {
			armAnimator.setDefault((mouseInLeft)?27:14);
		}
		else
			armAnimator.setDefault((mouseInLeft)?41:28);
	}

	
	@Override
	public void mouseClicked(MouseEvent e) {
		clicked=true;
	}
	
	
	@Override
	public void mouseReleased(MouseEvent e) {
		clicked=false;
		armAnimator.defaultAnimate();
	};
	
	
	@Override
	public void mouseMove(MouseEvent e) {
		mouse.setLocation(e.getX(), e.getY());
		mouseInLeft= e.getX()<xCenter;	
		if(lookDirection!=mouseInLeft) {
			lookDirection=mouseInLeft;
       		switch(selected_tool) {
       		case Empty_Hand:
       			armAnimator.setDefault((mouseInLeft)?13:0);
       			break;
       		case Sword:
       			armAnimator.setDefault((mouseInLeft)?55:42);
       			break;
       		case Pickaxe:
       			armAnimator.setDefault((mouseInLeft)?27:14);
       			break;
       		case Wall_Breaker:
       			break;
       		default:
       			armAnimator.setDefault((mouseInLeft)?41:28);
       			break;
       		}
		}	
	};
	
	
	@Override
	public void roundAction() {
		if(!board.isFocusOwner()) {
			board.requestFocus();
		}
		playerMove();
		if(clicked) {
			clickAction();
		}
		if(moved) {
			updateSubBackground();
		}
		if(!dust.isEmpty()) {
			particleAction(dust);
		}
		if(!blood.isEmpty()) {
			particleAction(blood);
		}
		if(!monster.isEmpty()) {
			for(Monster m:monster) {
				m.findDirection(player);
				m.move();
			}
		}
	}
	
	
	private void addParticle(LinkedList<Point> particle, int size) {
		int n=rand.nextInt(5)+5;
		for(int i=0;i<n;++i) {
			Point p=new Point(mouse.getX(),mouse.getY(),rand.nextDouble()*2+size,2-4*rand.nextDouble(),2-4*rand.nextDouble());
			particle.add(p);
		}
	}

	
	private void particleAction(LinkedList<Point> particle) {
		Iterator<Point> it=particle.iterator();
		while(it.hasNext()) {
			Point p=(Point) it.next();
			p.addLifeSpan();
			if(p.getLifeSpan()>10) {
				it.remove();
			}
			p.move();
		}
	}
	
	
	private void adjustParticle(boolean x, int m) {
		if(!blood.isEmpty()) {
			if(x) 
				blood.forEach(p->p.setX(p.getX()+m));
			else
				blood.forEach(p->p.setY(p.getY()+m));
		}
		if(!dust.isEmpty()) {
			if(x) 
				dust.forEach(p->p.setX(p.getX()+m));
			else
				dust.forEach(p->p.setY(p.getY()+m));
		}
		
	}

	
	private void clickAction() {
		switch(selected_tool) {
		case Empty_Hand:
			if(mouseInLeft) {
				armAnimator.animateSetting(7, 10, true);
			}
			else {
				armAnimator.animateSetting(3, 6, true);
			}
			break;
		case Sword:
			if(mouseInLeft) {
				armAnimator.animateSetting(49, 52, true);
			}
			else {
				armAnimator.animateSetting(45, 48, true);
			}
			attack();
			break;
		case Pickaxe:
		case Wall_Breaker:
			if(mouseInLeft) {
				armAnimator.animateSetting(21, 24, true);
			}
			else {
				armAnimator.animateSetting(17, 20, true);
			}
			removeBlock();
			break;
		case Dirt:
		case Rock:
		case Stone:
		case Wood:
			placeBlock();
			if(mouseInLeft) {
				armAnimator.animateSetting(35, 38, true);
			}
			else {
				armAnimator.animateSetting(31, 34, true);
			}
			break;
		case Wood_Wall:
		case Stone_Wall:
		case Window_Wall:
		case Leaf_Wall:
		case Touch:
		case Door1:
		case Door2:
		case Door3:
		case Door4:
			placeWall();
			if(mouseInLeft) {
				armAnimator.animateSetting(35, 38, true);
			}
			else {
				armAnimator.animateSetting(31, 34, true);
			}
			break;
		default:
			break;
		}
	}
	
	
	private void attack() {
		double x=(player.getX()+player.getWidthRadius())-(xCenter-mouse.getX());
		double y=(player.getY()+player.getHeightRadius())-(yCenter-mouse.getY());
		monster.forEach(m->{
			if(m.getLifeSpan()>0) {
				if(Math.abs(x-(m.getX()+m.getWidthRadius()))<=m.getWidthRadius() && Math.abs(y-(m.getY()+m.getHeightRadius()))<=m.getHeightRadius()) {
					m.attacked();
					addParticle(blood,m.getWidthRadius());
				}
			}
		});
	}
	
	
	private void removeBlock() {
		int x=getBlockValue((int)((player.getX()+player.getWidthRadius())-(xCenter-mouse.getX())));			
		int y=getBlockValue((int)((player.getY()+player.getHeightRadius())-(yCenter-mouse.getY())));	
		if(map.containsKey(x) && map.get(x).containsKey(y)) {
			if(selected_tool==Tool.Pickaxe) {
				addParticle(dust,1);
				Point p=map.get(x).get(y);
				p.setLifeSpan(p.getLifeSpan()-digPower);
				int hp=(int)(((double)p.getLifeSpan()/(double)p.getDefaultLifeSpan())*10);
				if(hp<=0) {
					map.get(x).remove(y);
					
					HashMap<Integer,HashMap<Integer,Integer>> wall=(x<blockMap_xCenter)?left_wall:right_wall;
					int newX=(x<blockMap_xCenter)?x:x-blockMap_xCenter;
					if(p.getDefaultLifeSpan()>=blockHP) {
						if(!wall.containsKey(newX) || !wall.get(newX).containsKey(y)) {
							addWall(newX, y, p.getDefaultLifeSpan(), wall);
							renderWall(x,y,p.getDefaultLifeSpan());	
						}
						else {
							clearMap(x,y, blockSize, blockSize);
							renderWall(x,y,wall.get(newX).get(y));
						}
					}
					else {
						clearMap(x,y, blockSize, blockSize);
					}
					moved=true;
					}
				else if(useImage){
					renderCrack(x,y,blockImage[354-hp]);
				}
			}
		}
		else if(selected_tool==Tool.Wall_Breaker) {
			HashMap<Integer,HashMap<Integer,Integer>> wall=(x<blockMap_xCenter)?left_wall:right_wall;
			int newX=(x<blockMap_xCenter)?x:x-blockMap_xCenter;
			if(wall.containsKey(newX) && wall.get(newX).containsKey(y)) {
				clearMap(x,y, blockSize, blockSize);
				wall.get(newX).remove(y);
			}
		}
	}
	
	
	private void placeWall() {
		int x=getBlockValue((int)((player.getX()+player.getWidthRadius())-(xCenter-mouse.getX())));			
		int y=getBlockValue((int)((player.getY()+player.getHeightRadius())-(yCenter-mouse.getY())));	
		if(!map.containsKey(x) || !map.get(x).containsKey(y)) {
			HashMap<Integer,HashMap<Integer,Integer>> wall=(x<blockMap_xCenter)?left_wall:right_wall;
			int newX=(x<blockMap_xCenter)?x:x-blockMap_xCenter;
			
			if(wall.containsKey(newX) && wall.get(newX).containsKey(y)) {
				clearMap(x,y, blockSize, blockSize);
				wall.get(newX).remove(y);
			}
	
			int v=0;
			switch(selected_tool) {
			case Stone_Wall:
				v+=6;
				break;
			case Leaf_Wall:
				v+=73;
				break;
			case Window_Wall:
				v+=70;
				break;
			case Wood_Wall:
				v+=305;
				break;
			case Touch:
				v+=115;
				break;
			case Door1:
				v+=116;
				break;
			case Door2:
				v+=139;
				break;
			case Door3:
				v+=117;
				break;
			case Door4:
				v+=140;
				break;
			default:
				break;
			}
			
			addWall(newX, y,v, wall);
			renderWall(x,y,v);
		}
	}
	
	
	private void placeBlock() {
		int x=getBlockValue((int)((player.getX()+player.getWidthRadius())-(xCenter-mouse.getX())));			
		int y=getBlockValue((int)((player.getY()+player.getHeightRadius())-(yCenter-mouse.getY())));	
		if(!map.containsKey(x) || !map.get(x).containsKey(y)) {
			if(x<player.getX()+player.getWidth() && x>player.getX()-(blockSize)) {
				if(y<player.getY()+player.getHeight() && y>player.getY()-(blockSize)) {
					return;
				}
			}
			int v=0;
			switch(selected_tool) {
			case Dirt:
				v=blockHP;
				break;
			case Rock:
				v=blockHP*3;
				break;
			case Stone:
				v=5;
				break;
			case Wood:
				v=4;
				break;
			default:
				break;
			}
			Point p=new Point(((x<blockMap_xCenter)?x:x-blockMap_xCenter),y);
			p.setDefaultLifeSpanAndLifeSpan(v);
			if(map.containsKey(x)) {			
				map.get(x).put(y,p);
			}
			else {
				HashMap<Integer,Point> innerMap=new HashMap<Integer,Point>();
				innerMap.put(y, p);
				map.put(x,innerMap);
			}
			renderBlock(x,y,v);
		}
	}
	
	
	@Override
	public void drawBack(Graphics2D g2d) {
		if(player.getY()<blockMap.getWidth()*0.55) {
			g2d.drawImage(background[0],0, 0,board.getWidth(),board.getHeight(),null);
		}
		else {
			g2d.drawImage(background[1],0, 0,board.getWidth(),board.getHeight(),null);
		}
		g2d.drawImage(sub_blockMap,0,0,board.getWidth(),board.getHeight(),null);		
		if(useImage) {
			g2d.drawImage(playerAnimator.getImage(), xCenter-player.getWidthRadius(),yCenter-player.getHeightRadius(),player.getWidth(),player.getHeight(), null);
			g2d.drawImage(armAnimator.getImage(), xCenter-player.getWidth(),yCenter-player.getHeightRadius(),player.getWidth()*2,player.getHeight(), null);
			monster.forEach(m->{
				if(m.getLifeSpan()>0) {
					int x=(int)(player.getX()-m.getX());
					int y=(int)(player.getY()-m.getY());
					g2d.drawImage(m.getAnimator().getImage(),(xCenter-x-m.getWidthRadius()),yCenter-y-m.getHeightRadius(),m.getWidth(),m.getHeight(),null);	
				}
			});
		}
		else {
			g2d.fill(new  Rectangle2D.Double(xCenter-player.getWidthRadius(),yCenter-player.getHeightRadius(),player.getWidth(),player.getHeight()));
			g2d.setColor(Color.GREEN);	
			monster.forEach(m->{
				if(m.getLifeSpan()>0) {
					double x=player.getX()-m.getX();
					double y=player.getY()-m.getY();
					g2d.fill(new  Rectangle2D.Double(xCenter-x-m.getWidthRadius(),yCenter-y-m.getHeightRadius(),m.getWidth(),m.getHeight()));	
				}
			});
		}
		if(!dust.isEmpty()) {
			g2d.setColor(Color.DARK_GRAY);
			dust.forEach(p->p.drawShape(g2d));
		}
		if(!blood.isEmpty()) {
			g2d.setColor(bloodColor);
			blood.forEach(p->p.drawShape(g2d));
		}
	}
	
	
	@Override
	public void drawFront(Graphics2D g2d) {
		g2d.setColor(Color.red);
		g2d.drawString("**Minecraft 2D**", (board.getWidth()/2)-40, 13);
		g2d.drawString("Area Zone: "+area_zone+","+(area_zone-1), board.getWidth()-130, 13);
		g2d.drawString("(x: "+(int)player.getX()+", y:"+(int)player.getY()+")", board.getWidth()-130, 26);
		for(int i=0; i<tools.length;++i) {
			g2d.drawString((i+1)+". "+tools[i], board.getWidth()-119, 55+(15*i));
		}
		g2d.setColor(selectColor);
		g2d.fillRect(board.getWidth()-119, 42+(15*selected_tool.ordinal()), 70, 15);
	}
	
	
	private void updateSubBackground(){
		sub_blockMap=blockMap.getSubimage((int)player.getX()+player.getWidthRadius()-xCenter,(int)player.getY()+player.getHeightRadius()-yCenter, board.getWidth(), board.getHeight());
	}

	
	private void renderBlock(int x, int y,int image) {
		Graphics2D g2d=(Graphics2D)blockMap.getGraphics();
		if(useImage) {
			if(image<blockHP){
				g2d.drawImage(blockImage[image], x, y,blockSize,blockSize,null);
			}
			else {
				switch(image) {
				case 1001:
					g2d.drawImage(blockImage[3], x, y,blockSize,blockSize,null);
					break;
				case 1000:
					g2d.drawImage(blockImage[2], x, y,blockSize,blockSize,null);
					break;
				case 3000:
					g2d.drawImage(blockImage[1], x, y,blockSize,blockSize,null);
					break;
				case 5000:
					g2d.drawImage(blockImage[47], x, y,blockSize,blockSize,null);
					break;
				}
			}
		}
		else {
			g2d.setColor(Color.DARK_GRAY);
			g2d.fillRect(x, y, blockSize, blockSize);
		}
	}
	
	
	private void renderCrack(int x, int y, BufferedImage image) {
		Graphics2D g2d=(Graphics2D)blockMap.getGraphics();
		g2d.drawImage(image, x, y,blockSize,blockSize,null);
	}


	private void renderWall(int x, int y, int image) {
		Graphics2D g2d=(Graphics2D)blockMap.getGraphics();
		if(useImage) {
			if(image<blockHP) {
				g2d.drawImage(blockImage[image], x, y,blockSize,blockSize,null);
			}
			else if(image<blockHP*3) {
				g2d.drawImage(wallImage[2], x, y,blockSize,blockSize,null);
			}
			else{
				g2d.drawImage(wallImage[1], x, y,blockSize,blockSize,null);
			}
		}
		else {
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillRect(x, y, blockSize, blockSize);
		}
	}
	
	
	private void addWall(int x, int y, int v, HashMap<Integer,HashMap<Integer,Integer>> wall) {
		if(wall.containsKey(x)) {
			wall.get(x).put(y, v);
		}
		else {
			HashMap<Integer,Integer> innerMap=new HashMap<Integer,Integer>();
			innerMap.put(y, v);
			wall.put(x, innerMap);
		}
		
	}

	
	private void clearMap(int x, int y, int width,int height) {
		Graphics2D g2d=(Graphics2D)blockMap.getGraphics();
		g2d.setBackground(transparentColor);
		g2d.clearRect(x, y, width, height);
	}
	
	
	private int getBlockValue(int n) {
		int remainder=n%blockSize;
		if(remainder!=0) {
			n-=remainder;
		}
		return n;
	}
	
	
	private void generateBlock(int start, int end) {		
		for(int x=start; x<end;x+=blockSize) {
			HashMap<Integer,Point> innerMap=new HashMap<Integer,Point>();
			boolean top=true;
			for(int y=blockMap_yCenter+(blockSize*(rand.nextInt(2)-1)); y<blockMap.getHeight();y+=blockSize) {
				Point p=new Point((x<blockMap_xCenter)?x:x-blockMap_xCenter,y,blockSize);
				if(y<blockMap.getHeight()*0.55) {
					if(top) {
						top=false;
						if(rand.nextInt(6)==0) {
							generateFlower(x,y-30);
						}
						p.setDefaultLifeSpanAndLifeSpan(blockHP+1);
					}
					else {
						p.setDefaultLifeSpanAndLifeSpan(blockHP);
					}
				}
				else {
					if(rand.nextInt(100)==0) {
						p.setDefaultLifeSpanAndLifeSpan(blockHP*5);
					}
					else {
						p.setDefaultLifeSpanAndLifeSpan(blockHP*3);
					}				
				}
				innerMap.put(y,p);
				renderBlock(x,y,p.getDefaultLifeSpan());
			}
			map.put(x,innerMap);
		}	
	}
	
	
	private void generateFlower(int x, int y) {
		boolean left=x<blockMap_xCenter;
		int pointX=(left)?x:x-blockMap_xCenter;
		HashMap<Integer, HashMap<Integer,Integer>> wall=(left)?left_wall:right_wall;
		if(rand.nextInt(6)==0) {  //tree
			int v=27;
			addWall(pointX,y,v,wall);
			renderWall(x,y,v);
			
			int newY=y-blockSize;
			int maxY=newY-blockSize;
			//add wood
			while(newY>=maxY) {
				addWall(pointX,newY,v,wall);
				renderWall(x,newY,v);
				newY-=blockSize;
			}
			//add leaf
			maxY=newY-(blockSize*2);
			v=74;
			while(newY>=maxY) {
				int n=(newY==maxY)?1:2;
				for(int i=-n;i<=n;++i) {
					addWall(pointX+(i*blockSize),newY,v,wall);
					renderWall(x+(i*blockSize),newY,v);
				}
				newY-=blockSize;
			}
		}
		else {
			int v=368+rand.nextInt(13);
			addWall(pointX,y,v,wall);
			renderWall(x,y,v);
		}
	}
	
	
	private void shiftAction(int zone1, int zone2, int start, int end, HashMap<Integer, HashMap<Integer, Integer>> wall, boolean left){
		save_map=new Thread(new Runnable() {
			@Override
			public void run() {
				saveMap(zone1,start,end,wall);
			}
			
		});
		save_map.start();	
		load_map=new Thread(new Runnable() {
			@Override
			public void run() {
				shift_zone(zone2,left);
			}
		});
		load_map.start();
	}
	

	private void shift_zone(int zone,boolean shift_left) {
		shifting=true;
		int start,end;
		if(shift_left) {
			start=blockMap_xCenter;
			end=blockMap.getWidth();
			left_wall=right_wall;
			right_wall=new HashMap<Integer,HashMap<Integer,Integer>>();
		}
		else {
			start=0;
			end=blockMap_xCenter;
			right_wall=left_wall;
			left_wall=new HashMap<Integer,HashMap<Integer,Integer>>();
		}
		for(int i=start;i<end;i+=blockSize) {
			map.put(i+((shift_left)?-(blockMap_xCenter):(blockMap_xCenter)),map.get(i));
		}
		renderMap(shift_left);
	
		int shiftValue=(shift_left)?-blockMap_xCenter:blockMap_xCenter;
		player.setX(player.getX()+shiftValue);
		monster.forEach(m->m.setX(m.getX()+shiftValue));
		
		File file = new File(fileURL+zone);
		if(file.exists() &&  !file.isDirectory()) {
			loadMap(file,start,end,!shift_left);
		}
		else {
			generateBlock(start,end);
		}
		shifting=false;
	}
	
	
	private void renderMap(boolean shift_left) {
		Graphics2D g2d=(Graphics2D)blockMap.getGraphics();
		g2d.setColor(Color.white);
		if(shift_left) {
			BufferedImage sub=blockMap.getSubimage(blockMap_xCenter, 0,blockMap_xCenter, blockMap.getHeight());
			clearMap(0, 0,blockMap_xCenter, blockMap.getHeight());
			g2d.drawImage(sub,0,0,blockMap_xCenter,blockMap.getHeight(),null);	
			clearMap(blockMap_xCenter, 0,blockMap_xCenter, blockMap.getHeight());
		}
		else {
			BufferedImage sub=blockMap.getSubimage(0, 0, blockMap_xCenter, blockMap.getHeight());
			clearMap(blockMap_xCenter,0,blockMap_xCenter, blockMap.getHeight());
			g2d.drawImage(sub,blockMap_xCenter,0,blockMap_xCenter,blockMap.getHeight(),null);
			clearMap(0, 0,blockMap_xCenter, blockMap.getHeight());
		}
		g2d.setColor(Color.black);
	}
	
	
	private void loadMap(File file, int start,int end, boolean shift_left) {
		try {
			BufferedReader br= new BufferedReader(new FileReader(file));
			String w=br.readLine();
			if(!w.equals("")) {
				String[] walls=w.split("\\|");
				for(String wall:walls) {
					String[] data=wall.split(",");
						int x=Integer.parseInt(data[0]);
						int y=Integer.parseInt(data[1]);
						int v=Integer.parseInt(data[2]);
						renderWall(((shift_left)?x:x+blockMap_xCenter),y,v);
						HashMap<Integer,HashMap<Integer,Integer>> wall_map=(shift_left)?left_wall:right_wall;
						addWall(x,y,v,wall_map);
				}
			}
  			while(start<end) {
  				HashMap<Integer,Point> innerMap=new HashMap<Integer,Point>();
  				String[] blocks=br.readLine().split("\\|");
  				for(String block:blocks) {
  					String[] data=block.split(",");
  					Point p=new Point(((start<blockMap_xCenter)?start:start-blockMap_xCenter),Integer.parseInt(data[0]));
  					p.setDefaultLifeSpanAndLifeSpan(Integer.parseInt(data[1]));
  					innerMap.put((int)p.getY(),p);
  					renderBlock(start,(int)p.getY(),p.getDefaultLifeSpan());
  				}
  				map.put(start,innerMap);
  				start+=blockSize;
  			}
			br.close();
		}catch(IOException e){
			e.printStackTrace ();
		}
	}

	
	private void saveMap(int zone, int start, int end, 	HashMap<Integer,HashMap<Integer,Integer>> wall) {
  		try{ 
  			HashMap<Integer,HashMap<Integer,Point>> copy_map=new HashMap<Integer,HashMap<Integer,Point>>(map);
  			BufferedWriter bw=new BufferedWriter(new FileWriter(fileURL+zone));
  			
  			Iterator<Entry<Integer, HashMap<Integer, Integer>>> wall_it=wall.entrySet().iterator();
  			while(wall_it.hasNext()) {
  				Entry<Integer, HashMap<Integer, Integer>> x=wall_it.next();
  				Iterator<Entry<Integer, Integer>> inner_it=x.getValue().entrySet().iterator();
  				while(inner_it.hasNext()) {
  					Entry<Integer,Integer> y=inner_it.next();
  					bw.write((x.getKey()+","+y.getKey()+","+y.getValue()+"|"));	
  				}
  			}
  			bw.newLine();
  			while(start<end) {
				if(copy_map.containsKey(start)) {
					Iterator<Entry<Integer, Point>> it=copy_map.get(start).entrySet().iterator();
					while(it.hasNext()) {
						Point p=it.next().getValue();
						bw.write((int)p.getY()+","+(p.getDefaultLifeSpan()));
						bw.write("|");	
					}
				}
				bw.newLine();
				start+=blockSize;
			}
				bw.close();
  		}catch(IOException e){
  	 	e.printStackTrace ();
  		}
	}
	
	
	private class MonsterHandler implements Runnable{
		private boolean run=true;
		private int spawnCD;
		@Override
		public void run() {
			while(run) {
				if(!monster.isEmpty()) {
					Iterator<Monster> it=monster.iterator();
					while(it.hasNext()) {
						Monster m=it.next();
						if(m.getLifeSpan()<=0) {
							if(!shifting) {
								it.remove();
							}
						}
						else {
							m.findDirection(player);
							m.move();
						}
					}
				}
				else {
					if(spawnCD>=100) {
						//spawn
						if(!monsterAnimator[0].isRunning() || !monsterAnimator[1].isRunning()) {
							for(Animator m:monsterAnimator) m.animateStart();
						}
						int spawnNumber=rand.nextInt(4);
						for(int i=0; i<spawnNumber;++i) {
							double spawnX=board.getWidth()+rand.nextInt(board.getWidth()/2);
							Monster m=new Monster(player.getX()+((rand.nextBoolean())?-spawnX:spawnX),player.getY()-(blockSize*5),player.getWidth(),player.getHeight(),50);
							if(useImage) m.setAnimator(monsterAnimator[0]);
							monster.add(m);
						}
						spawnCD=0;
					}
					else {
						if(monsterAnimator[0].isRunning() || monsterAnimator[1].isRunning()) {
							for(Animator m:monsterAnimator) m.animateStop();
						}
						spawnCD++;
					}
				}
				try {
					Thread.sleep(board.getTimerDelay());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
		}
		
		
		private void stop() {
			run=false;
		}
	}
	
	
	private class Monster extends Player{
		private double direction;
		private double previousX;
		private int attackedCD;
		
		private Monster(double x, double y, int width, int height, int hp) {
			super(x, y, width, height);
			this.setLifeSpan(hp);
		}
		
		@Override
		public void move() {
			double d=Math.abs(direction);
			if(d<board.getWidth()*3) {
				if(d>(this.getWidth())) {
					if(previousX==this.getX() && this.getUseValue()==0) {
						this.setUseValue(45);  //jump when get blocked
					}
					previousX=this.getX();
					this.move_LeftRight();
				}
				this.move_UpDown();
				}
			else this.setLifeSpan(0);
		}
		
		private void attacked() {
			attackedCD=50;
			if(this.getUseValue()<=0)
				this.setUseValue(25);
			this.reduceLifeSpan();
		}
		
		private void findDirection(Player p) {
			direction=p.getX()-this.getX();
			if(attackedCD>0) {
				direction=(direction>0)?-(2*this.getWidth()):2*this.getWidth();
				attackedCD--;
			}
		}
		
		private void move_LeftRight() {
			if(direction<0) {
				if(useImage && attackedCD<=0)this.setAnimator(Minecraft2D.this.monsterAnimator[1]);
				double newX=this.getX()-Minecraft2D.this.monsterSpeed;
				if(!Minecraft2D.this.leftORright_barrier(this,(int)newX)) {
		        	//playerAnimator.animateSetting(3, 4, true);;
					this.setX(newX);
				}
				else {
					if(this.getX()%Minecraft2D.this.blockSize!=0) {
						this.setX(Minecraft2D.this.getBlockValue((int)this.getX()));
					}
				}
			}				
			else{	
				if(useImage && attackedCD<=0)this.setAnimator(Minecraft2D.this.monsterAnimator[0]);
				double newX=this.getX()+Minecraft2D.this.monsterSpeed;
				if(!Minecraft2D.this.leftORright_barrier(this,(int)(newX+this.getWidth()))) {
		        	//playerAnimator.animateSetting(1, 2, true);
					this.setX(newX);
				}
				else {
					if((this.getX()+this.getWidth())%Minecraft2D.this.blockSize!=0) {
						this.setX(Minecraft2D.this.getBlockValue((int)this.getX()+this.getWidth()-1)+Minecraft2D.this.blockSize-this.getWidth());
					}
				}
			}
		}

		private void move_UpDown() {
			if(this.getUseValue()>1) {  //jumping
					double newY=this.getY()-(Minecraft2D.this.monsterSpeed*2);
					if(!Minecraft2D.this.upperORlower_barrier(this,(int)newY)) {
						this.setY(newY);
						this.reduceUseValue(); 
					}
					else this.setUseValue(1);  //stop jumping
			} 
			else{  //falling
				double newMovement=Minecraft2D.this.monsterSpeed*2;
				double newY=this.getY()+newMovement;
				if(!Minecraft2D.this.upperORlower_barrier(this,(int)(newY+this.getHeight()))) {
					if(this.getUseValue()==0) { //jump if jump not used
						this.setUseValue(25);
					}
					else {
						this.setY(newY);
					}
				}
				else{
					//make sure wont stuck into the block
					if((this.getY()+this.getHeight())%Minecraft2D.this.blockSize!=0) {
						this.setY(Minecraft2D.this.getBlockValue((int)this.getY()+this.getHeight()-1)+Minecraft2D.this.blockSize-this.getHeight());
					}
					this.setUseValue(0); //reset jump
				}
			}
		}
	}
}

