package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
/*import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;*/
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JOptionPane;

import object.Board;
import object.Point;
import tool.Helper;
import tool.PathCalculator;

class SPBLXS extends GameSetter {

	/*----------------GLOABL VARIABLES----------------*/
	private Thread secondCounter = new Thread();
	private Mouse mouse = new Mouse();
	private boolean secCount = true;
	private final static Color bldCanvasColor = new Color(235, 34, 37, 180);
	private final static Color energyCanvasColor = new Color(34, 225, 235, 180);
	private final static Color flyCanvasColor = new Color(37, 8, 88, 180);

	private final static Font gameFont = new Font("TimesRoman", Font.BOLD, 14);

	private BufferedImage blockCanvas;

	private int blockSize = 15; // size of each block. Make sure all speed cannot greater than block size
	private double movementSpeed = blockSize / 5; // movement speed
	private double jumpFuel = 300; // jump power

	private final Weapon[] playerWeaponList = {

			// player weapon (speed, size, dmg,travelRange, cooldown, consume, hp, color)
			// rifle
			new Weapon(movementSpeed * 2, blockSize / 4, 5, blockSize * 5, 150, 1, 1, Color.CYAN),
			// snipe
			new Weapon(movementSpeed * 4, blockSize / 2, 20, blockSize * 10, 1000, 30, 3, Color.CYAN),
			// super weapon
			new Weapon(blockSize / 2, blockSize * 2, 20, blockSize * 30, 20, 1, 5, Color.BLACK) };

	private int initEnergy = 2000;
	private int energy = initEnergy; // consuming energy
	private int playerHP = 400; // player health
	private int digpower = 5; // digging power
	private int blockhp = 10; // default block health
	private boolean energyShield = false; // use energy to replace hp

	private final Point mapSize = new Point(400, 400);
	private final Point spawnPoint = new Point(mapSize.getX() * blockSize / 2, mapSize.getY() * blockSize / 2.2);
	private final static String[] toolName = { "rifle", "sniper", "hack weapon" };

	private Entity player; // player
	private int tool = 1; // selected tool (e.g. block destroyer, gun...)
	private Block blockMap[][]; // data structure to store each block's data
	// var animate; //animating
	// var blockCanvas; //block canvas for block background
	// var blockCtx; //block context for drawing

	private LinkedList<Bullet> bulletList = new LinkedList<Bullet>(); // bullet data structure
	private LinkedList<Obj> lootList = new LinkedList<Obj>(); // loots
	private LinkedList<Obj> bloodList = new LinkedList<Obj>();
	private LinkedList<Entity> enemyList = new LinkedList<Entity>(); // enemy list
	private int enemySpawnSize = 4;
	private int spawn = enemySpawnSize;
	private boolean wait = false;
	private boolean boss = false;

	// (speed, size, dmg, travelRange, cooldown, consume, hp, color)
	// enemy attribute
	private final Weapon[] enemyWeapon = {
			// enemy1
			new Weapon(movementSpeed, blockSize / 4, 5, blockSize * 40, 600, 0, 2, Color.WHITE),
			// enemy2
			new Weapon(movementSpeed * 1.4, blockSize / 2, 5, blockSize * 40, 1500, 0, 5, Color.white),
			// enemy3
			new Weapon(movementSpeed, blockSize * 3, 5, blockSize * 40, 100, 0, 5, Color.BLUE),
			// enemy3
			new Weapon(movementSpeed * 3, blockSize / 3, 2, blockSize * 50, 1, 0, 4, Color.BLUE),
			// enemy3
			new Weapon(movementSpeed * 0.8, blockSize * 8, 100, blockSize * 60, 4000, 0, 300, Color.BLUE) };

	private long lastLoop = System.currentTimeMillis(); // use to calculate fps
	private long thisLoop = lastLoop; // use to calculate fps
	private int fps; // measure frame rate per second

	private int surviveTime = 0; // second player survived
	private boolean godMode = false; // god mode allow player not dying
	private boolean die = false;

	public SPBLXS(Board board) {
		super(board);
		// TODO Auto-generated constructor stub
	}

	/*----------------READY EVENT----------------*/
	@Override
	public void gameClear() {
		if (secondCounter.isAlive())
			secCount = false;
		gameReset();
	}

	@Override
	public Point getGeneratedPoint(int width, int height, double size) {
		return new Point(rand.nextInt(width), rand.nextInt(height), size, 0, 0);
	}

	public void loadSetting() {
		/*
		 * try{ BufferedReader br= new BufferedReader(new
		 * FileReader("config/game_config/snake/sk_config")); br.readLine(); String[]
		 * input=br.readLine().split("\\|");
		 * tailIncreaseNumber=(int)Math.ceil(Double.parseDouble(input[0]));
		 * snakeSize=(int)Math.ceil(Double.parseDouble(input[1])); br.close();
		 * }catch(IOException|NullPointerException e){
		 * JOptionPane.showMessageDialog(null,
		 * "\"config/game_config/snake/sk_config\" not found"); }
		 */
	}

	@Override
	public void gameStart() {
		board.clearPoint();
		/*
		 * loadSetting(); player=new
		 * Player(board.getWidth()/2,board.getHeight()/2,snakeSize); gameReset();
		 */
		// create player

		player = new Entity(spawnPoint, blockSize, blockSize * 2, playerHP, jumpFuel, digpower, playerWeaponList[0],
				mouse, movementSpeed, "player", Color.CYAN);
		// create blockCanvas
		newBlockCanvas();

		// generate block
		generateMap(0, (int) mapSize.getX());

		// draw the game
		// draw();
		// start counting
		secondCounter();
	};

	@Override
	public void gameReset() {
		bulletList.forEach(b -> b.destroy());
		bulletList.clear();
		lootList.forEach(b -> b.destroy());
		lootList.clear();
		enemyList.forEach(b -> b.destroy());
		enemyList.clear();
		player.hp = playerHP;
		energy = initEnergy;
		player.p.setLocation(spawnPoint.getX(), spawnPoint.getY());
		surviveTime = 0;
		score = 0;
		player.moved = true;
		enemySpawnSize = 4;
		spawn = enemySpawnSize;
		wait = false;
		boss = false;
	};

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_D: // d
			player.right = true;
			break;
		case KeyEvent.VK_S: // s
			player.down = true;
			break;
		case KeyEvent.VK_A: // a
			player.left = true;
			break;
		case KeyEvent.VK_SPACE: // space
		case KeyEvent.VK_W: // w
			player.up = true;
			break;
		case KeyEvent.VK_T: // t
			if (energyShield) {
				energyShield = false;
			} else if (energy >= 300) {
				// only active if energy is over 300
				energyShield = true;
			}
			break;
		case KeyEvent.VK_1:// 1
		case KeyEvent.VK_Q: // q
			tool = 1;
			player.weapon = playerWeaponList[0];
			break;
		case KeyEvent.VK_2:// 2
		case KeyEvent.VK_E: // e
			tool = 2;
			player.weapon = playerWeaponList[1];
			break;
		case KeyEvent.VK_K:
			if (indexConvert(player.p.getX(), mapSize.getX()) >= indexConvert(player.p.getX() + player.width,
					mapSize.getX()) && player.down) {
				player.weapon = playerWeaponList[2];
				tool = 3;
			}
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_W:
			this.player.up = false;
			break;
		case KeyEvent.VK_S:
			this.player.down = false;
			break;
		case KeyEvent.VK_A:
			this.player.left = false;
			break;
		case KeyEvent.VK_D:
			this.player.right = false;
			break;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (board.getTimer().isRunning()) {
			// System.out.println(e.getButton());
			player.click = e.getButton();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		player.click = -1;
	};

	@Override
	public void mouseMove(MouseEvent e) {
		mouse.p.setLocation(e.getX(), e.getY());
	};

	@Override
	public void playerMove() {
		double newX, newY;
		// for player walk left or right
		if (player.left || player.right) {
			// create a new x position based on direction.
			newX = (player.left) ? player.p.getX() - player.speed : player.p.getX() + player.speed;
			// teleport player to other side if player walk out of the map
			if (newX >= blockCanvas.getWidth()) {
				newX -= blockCanvas.getWidth();
			} else if (newX < 0) {
				newX += blockCanvas.getWidth();
			}
			// if there is new barrier set player x position to new x position
			if (!barrier_LeftRight(player, newX, player.left)) {
				player.p.setX(newX);
				// player moved
				player.moved = true;

			}
		}
		// update block player occupied
		if (player.moved) {
			updateOccupied(player, player.halfWidth, player.halfHeight);
		}

		// jump action
		if (player.up && player.jf > 0) {

			newY = player.p.getY() - player.speed;
			// check if there is a barrier on top
			if (!barrier_UpDown(player, newY - player.halfHeight, false)) {
				// player moved
				player.moved = true;
				// set new position
				player.p.setY(newY);
				// decrease jump time
			}
			player.jf--;
		}
		// test if player will fall if player is not jumping and has moved
		else if (player.moved) {
			// falling speed
			newY = player.p.getY() + player.speed;
			if (!barrier_UpDown(player, (newY + player.halfHeight), true)) {
				player.p.setY(newY);

			} else {
				// reset jump
				player.recoverJump();
				player.moved = false;
				updateOccupied(player, player.halfWidth, player.halfHeight);
			}
		} else
			player.recoverJump();

	}

	@Override
	public void roundAction() {
		if (!board.isFocusOwner()) {
			board.requestFocus();
		}
		gameLoop();
	};

	@Override
	public void gameOver() {
		int n = JOptionPane.showConfirmDialog(null,
				"Game Over!!\nYour Score: " + score + "\nYour Level: " + getLevel() + "\n\nDo You Want To Restart?",
				"Restart?", JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.YES_OPTION) {
			gameReset();
		} else
			board.refresh();
	}

	@Override
	public void drawBack(Graphics2D g2d) {
		draw(g2d);
		/*
		 * g2d.drawString("**Snake**", (board.getWidth()/2)-40, 13);
		 * g2d.drawString("Your Score: "+getScore(), board.getWidth()-100, 13);
		 * g2d.drawString("Game Level: "+getLevel(), board.getWidth()-100, 26);
		 * g2d.drawString("Tail Length: "+tailSize, board.getWidth()-100, 39);
		 * g2d.setColor(Color.green.darker()); for(Point p:tail) { p.drawShape(g2d); }
		 * player.drawShape(g2d);
		 */
	};

	// generate new map
	private void generateMap(int start, int end) {
		int horizontalLine = (int) Math.round(mapSize.getY() / 1.8);
		blockMap = new Block[(int) mapSize.getX()][(int) mapSize.getY()];
		for (int x = start; x < end; x++) {

			int adjustLine = (int) (horizontalLine + Math.round(1 - Math.random() * 2));
			if (adjustLine > mapSize.getY() * 0.1 && adjustLine < mapSize.getY() * 0.8) {
				horizontalLine = adjustLine;
			}
			// generate each vertical block from half
			for (int y = horizontalLine; y < mapSize.getY(); y++) {

				// different layer different block
				int hp;
				Color color;
				if (y < mapSize.getY() * (Math.random() * (0.7 - 0.6) + 0.6)) {
					hp = blockhp;
					color = Color.LIGHT_GRAY;
				} else if (y < mapSize.getY() * (Math.random() * (0.85 - 0.7) + 0.7)) {
					hp = blockhp * 2;
					color = Color.LIGHT_GRAY.darker();
				} else if (y < mapSize.getY() * (Math.random() * (0.9 - 0.87) + 0.87)) {
					hp = blockhp * 4;
					color = Color.LIGHT_GRAY.darker().darker();
				} else {
					hp = blockhp * 500;
					color = Color.LIGHT_GRAY.darker().darker().darker();
				}

				Point p = new Point(x * blockSize, y * blockSize);
				// create block object
				Obj obj = new Obj(p, blockSize, hp, color);
				// create new block, 2% chance to contain loot inside the block
				Block block = (Math.round(Math.random() * 100) >= 98) ? new Block(obj, p, "loot")
						: new Block(obj, p, "");
				// draw block into block canvas
				renderBlock(obj.p, obj.color);
				// insert block
				blockMap[x][y] = block;
			}
		}
	}

	// count every one second
	private void secondCounter() {

		/*
		 * setInterval(function () { //only do if is animating if (animate !== null) {
		 * //energy consume *5 when open energy shield if (energyShield) { energy -= 6;
		 * }else if (energy >= player.hp) { energy--; }
		 * 
		 * if (player.hp < 400) { player.hp++; } surviveTime++; } }, 1000);
		 */
		secondCounter = new Thread(() -> {

			while (secCount) {
				if (board.getTimer().isRunning()) {
					// energy consume *5 when open energy shield
					if (energyShield) {
						energy -= 6;
					} else if (energy >= player.hp) {
						energy--;
					}

					if (player.hp < 400) {
						player.hp++;
					}
					surviveTime++;
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		});
		secondCounter.start();

	}

	/*----------------GAME CONTENT----------------*/

	private void gameLoop() {
		// loop game only if
		if (godMode || (!die && player.hp > 0)) {
			// update fps
			updateFPS();
			// player move action
			playerMove();
			// player click action
			playerClick();
			// refresh canvas;
			// draw();
			// bullet event and render bullet
			bulletMove();
			// loot event
			lootMove();
			// spawn enemy
			spawnEnemy();
			// move enemy
			enemyMove();
			// move blood
			bloodMove();
		} else {
			gameOver();
		}
	}

	// update fps
	private void updateFPS() {
		double divide = thisLoop - lastLoop;
		if (divide == 0)
			divide = 1;
		fps = (int) (1000 / divide);
		lastLoop = thisLoop;
		thisLoop = System.currentTimeMillis();
	}

	// convert block coordinate to map index
	private int indexConvert(double val, double limit) {
		int index = (int) Math.round(val / blockSize);
		if (index < 0) {
			index = (int) (limit + index);
		} else if (index >= limit) {
			index -= limit;
		}
		return index;
	}

	/*----------------ALL CLICK private void----------------*/

	// player click action
	private void playerClick() {
		// System.out.println(player.click);
		switch (player.click) {
		// left click
		case MouseEvent.BUTTON1:
			// tool using
			switch (tool) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				// shooting from player to mouse location
				shootBullet(player, mouse.mapXY(), false);
				break;
			}
			break;
		// middle click
		case MouseEvent.BUTTON2:
			// place block
			placeObject();
			break;
		// right click
		default:
			destroyObject();
			break;
		// right click
		}
	}

	// shoot bullet event
	private void shootBullet(Entity shooter, Point target, boolean inaccurate) {
		if (!shooter.fire) {
			Point velocity;
			// shoot inaccurately
			if (inaccurate) {
				// accuracy base on this number, the larger number the lower accuracy
				double accu = blockSize * 4;
				double accurX;
				double accurY = shooter.p.getY() + ((accu) - (Math.random() * accu * 2));
				if (Math.abs(target.getX() - shooter.p.getY()) > blockCanvas.getWidth() / 2) {
					double mx = (shooter.p.getX() > target.getX()) ? shooter.p.getX() - blockCanvas.getWidth()
							: shooter.p.getX() + blockCanvas.getWidth();
					accurX = mx + ((accu) - (Math.random() * accu * 2));
				} else {
					accurX = shooter.p.getX() + ((accu) - (Math.random() * accu * 2));
				}
				Point newp = new Point(accurX, accurY);
				velocity = PathCalculator.getVelocity(newp, target, shooter.weapon.speed);
			} else {
				// use getVelocity() from func.js to get velocity between shooter and target
				// point
				velocity = PathCalculator.getVelocity(shooter.p, target, shooter.weapon.speed);
			}

			// System.out.println(velocity.getX()+" "+velocity.getY());
			// System.out.println(velocity.getX());
			// create a bullet from shooter position and use shooter's bullet property
			Bullet bullet = new Bullet(shooter.p, velocity, shooter.weapon, shooter.type);
			// add bullet into the list
			bulletList.add(bullet);
			shooter.setCD();
			// decrease energy if shooter is player
			if (shooter == player) {
				energy -= shooter.weapon.consume;
			}
		}
	}

	// player place object event
	private void placeObject() {
		// check click range
		double centerX = board.getWidth() / 2;
		double centerY = (board.getHeight() / 2) - blockSize / 2;
		// make sure clicking location is inside block range of player
		if (Math.abs(mouse.p.getX() - centerX) <= blockSize * 5.5
				&& Math.abs(mouse.p.getY() - centerY) <= blockSize * 5.5) {
			Point mapmouse = mouse.mapXY();

			// get mosue x and y based on player location
			int x = indexConvert(mapmouse.getX(), mapSize.getX());
			int y = indexConvert(mapmouse.getY(), mapSize.getY());
			// get block
			Block mapBlock = blockMap[x][y];
			// call success private void if there is a block in this coordinate
			if (mapBlock != null) {
				if (mapBlock.stObj == null && !mapBlock.objs.contains(player)) {
					Obj obj = new Obj(new Point(x * blockSize, y * blockSize), blockSize, blockhp * 2,
							Color.LIGHT_GRAY);
					mapBlock.stObj = obj;
					renderBlock(obj.p, Color.LIGHT_GRAY);
					energy -= 2;
				}
			}
			// call fail private void if fail is defined and there is not block in this
			// coordinate
			else {
				Point p = new Point(x * blockSize, y * blockSize);
				// if this block is empty create the block with the object added
				Obj obj = new Obj(p, blockSize, blockhp * 2, Color.LIGHT_GRAY);
				blockMap[x][y] = new Block(obj, p);
				renderBlock(obj.p, Color.LIGHT_GRAY);
				energy -= 2;

			}
		}
	}

	private void destroyObject() {
		// check click range
		double centerX = board.getWidth() / 2;
		double centerY = (board.getHeight() / 2) - blockSize / 2;
		// make sure clicking location is inside block range of player
		if (Math.abs(mouse.p.getX() - centerX) <= blockSize * 5.5
				&& Math.abs(mouse.p.getY() - centerY) <= blockSize * 5.5) {
			Point mapmouse = mouse.mapXY();
			// get mosue x and y based on player location
			int x = indexConvert(mapmouse.getX(), mapSize.getX());
			int y = indexConvert(mapmouse.getY(), mapSize.getY());
			// get block
			Block mapBlock = blockMap[x][y];
			// destroy static object if there is any
			if (mapBlock != null && mapBlock.stObj != null) {
				mapBlock.stObj.hp -= player.digpower;
				if (mapBlock.stObj.hp <= 0) {
					clearBlock(mapBlock.p);
					mapBlock.stObj = null;
					// trigger player move event
					player.moved = true;
					// add loot if this block has loot
					if (mapBlock.background.equals("loot")) {
						// pass block
						addLoot(mapBlock, 0);
					}
				}
			}
		}
	}

	/*----------------MOVEMENT COLLISION----------------*/

	// detect upper or lower barrier
	private boolean barrier_UpDown(Entity obj, double newY, boolean land) {
		// upper and lower limit
		if (newY > blockCanvas.getHeight() * 0.9 || newY < blockCanvas.getHeight() * 0.1) {
			return true;
		}
		// index of min x coordinate
		int minxi = indexConvert(obj.p.getX() - obj.halfWidth + 1, mapSize.getX());
		// index of max x coordinate
		int maxxi = indexConvert(obj.p.getX() + obj.halfWidth - 1, mapSize.getX());
		// index of y coordinate
		int yi = indexConvert(newY, mapSize.getY());
		// if left and right is inside same block
		if (minxi == maxxi) {
			// return true if block has a static object
			if (checkBlock(minxi, yi)) {
				if (land) {
					// make sure player wont stuck into the block
					obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
				} else {
					obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
				}
				return true;
			}
		} else {
			// extreme case when minii is greater than maxxi when cross boundary
			if (minxi >= maxxi) {
				while (minxi != maxxi + 1) {
					// console.log(minxi+" "+maxxi);
					// return true if there is a barrier
					if (checkBlock(minxi, yi)) {
						if (land) {
							// make sure player wont stuck into the block
							obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
						} else {
							obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
						}
						return true;
					} else {
						minxi++;
						if (minxi >= mapSize.getX()) {
							minxi = 0;
						}
					}
				}
			} else {
				// travel from top to bottom to see if any body part collide with an object
				while (minxi <= maxxi) {
					// return true if there is a barrier
					if (checkBlock(minxi, yi)) {
						if (land) {
							// make sure player wont stuck into the block
							obj.p.setY((yi * blockSize) - (obj.halfHeight) - blockSize / 2);
						} else {
							obj.p.setY((yi * blockSize) + (obj.halfHeight) + blockSize / 2);
						}
						return true;
					} else {
						minxi++;
					}
				}
			}
		}
		return false;
	}

	// check if block has static object
	private boolean checkBlock(int x, int y) {
		return blockMap[x][y] != null && blockMap[x][y].stObj != null;
	}

	// detect left or right barrier
	private boolean barrier_LeftRight(Entity obj, double newX, boolean left) {
		// get map x index;
		int xi = indexConvert(((left) ? newX - obj.halfWidth : newX + obj.halfWidth), mapSize.getX());
		// check if there is vertical blocks in this horizontal position
		if (blockMap[xi] != null) {
			// get obj's top y index
			int minyi = indexConvert((obj.p.getY() + 1 - obj.halfHeight), mapSize.getY());
			// get obj's bottom y index
			int maxyi = indexConvert((obj.p.getY() - 1 + obj.halfHeight), mapSize.getY());
			// if top and bottom is in same block
			if (minyi == maxyi) {
				// return true is there is no static object else return false
				if (checkBlock(xi, minyi)) {
					// adjust obj coordinate so it wont stuck into the block
					obj.p.setX((xi * blockSize) + ((left) ? blockSize : -blockSize));
					updateOccupied(obj, obj.halfWidth, obj.halfHeight);
					return true;
				}
			} else {
				// travel from top to bottom to see if any body part collide with an object
				while (minyi <= maxyi) {
					// return true if there is a barrier
					if (checkBlock(xi, minyi)) {
						// adjust obj coordinate so it wont stuck into the block
						obj.p.setX((xi * blockSize) + ((left) ? blockSize : -blockSize));
						updateOccupied(obj, obj.halfWidth, obj.halfHeight);
						return true;
					}
					minyi++;
				}
			}
		}
		// return no barrier
		return false;
	}

	// update the block player occupied
	private void updateOccupied(Obj obj, double width, double height) {

		int left = indexConvert((obj.p.getX() - width), mapSize.getX());
		int right = indexConvert((obj.p.getX() - 1 + width), mapSize.getX());
		int top = indexConvert(obj.p.getY() - height, mapSize.getY());
		int bot = indexConvert((obj.p.getY() - 1 + height), mapSize.getY());

		// get all block player occupied
		LinkedList<Block> newBlocks = new LinkedList<Block>();

		// extreme case when crossing border
		if (left > right) {
			while (left != right + 1) {
				int tempTop = top;
				while (tempTop <= bot) {
					if (blockMap[left][tempTop] == null) {
						blockMap[left][tempTop] = new Block(null, new Point(left * blockSize, tempTop * blockSize));
					}
					newBlocks.add(blockMap[left][tempTop]);
					tempTop++;
				}
				left++;
				if (left >= mapSize.getX()) {
					left = 0;
				}
			}
		} else {
			while (left <= right) {
				int tempTop = top;
				while (tempTop <= bot) {
					if (blockMap[left][tempTop] == null) {
						blockMap[left][tempTop] = new Block(null, new Point(left * blockSize, tempTop * blockSize));
					}
					newBlocks.add(blockMap[left][tempTop]);
					tempTop++;
				}
				left++;
			}
		}
		// update
		obj.updateBlock(newBlocks);
	}

	/*-----------------ENEMY EVENT----------------*/

	// generate enemy
	private void spawnEnemy() {
		if (enemyList.isEmpty() && wait) {
			wait = false;
			boss = false;

			Helper.setTimeout(() -> {
				enemySpawnSize += 1 + (Math.round(Math.random() * 3));
				spawn = enemySpawnSize;
			}, 4000);
		}

		// spawn boss
		if (surviveTime > 1 && (surviveTime == 150 || surviveTime % 421 == 0) && !boss) {
			boss = true;
			
	
			Point p = new Point(player.p.getX(), player.p.getY() - ((blockSize * 30)));
	
			int weapIndex = (int) (2 + Math.round(Math.random() * 2));
	
			Entity enemy = new Entity(p, blockSize * 8, blockSize * 2, 1800, 30, 0, enemyWeapon[weapIndex], player,
					movementSpeed, "eboss", Color.white);
			enemy.click = (int) (200 * Math.random() + 300);
			enemy.jf = 1;	
			enemyList.push(enemy);
		}

		if (surviveTime >= 5 && spawn > 0) {

			wait = true;
			boolean allowType2 = false;
			// spawn fly enemy when survive time is greater than 100 sec
			if (surviveTime >= 100)
				allowType2 = true;

			Point p = new Point(player.p.getX() + ((board.getWidth()) - (Math.random() * board.getWidth() * 2)),
					player.p.getY() - ((blockSize * 4) + blockSize * Math.random() * 20));
			String type;
			Weapon weapon;
			double width;
			double height;
			int hp;
			// use as block range
			double range = blockSize * ((Math.random() * 15) + 10);
			double speed = movementSpeed * (0.5 + Math.random() * 0.6);

			if (rand.nextBoolean() && allowType2) {

				type = "enemy2";
				weapon = enemyWeapon[1];
				width = blockSize;
				height = blockSize;
				hp = 20;

			} else {
				type = "enemy1";
				weapon = enemyWeapon[0];
				width = blockSize;
				height = blockSize * 2;
				hp = 25;
			}

			Entity enemy = new Entity(p, width, height, hp, range, 0, weapon, player, speed, type, Color.WHITE);
			enemy.click = (int) (200 * Math.random() + 300);
			enemy.jf = 1;
			enemyList.add(enemy);
			spawn--;
		}
	}

	// move enemy move and shoot
	private void enemyMove() {
		if (!enemyList.isEmpty()) {

			enemyList.forEach(enemy -> {
				switch (enemy.type) {
				case "enemy1":
					// move type 1 enemy
					move_type1(enemy);
					break;
				case "enemy2":
				case "eboss":
					// move type 1 enemy
					move_type2(enemy);
				}
				// make enemy shoot bullet or stop shooting randomly
				if (enemy.click <= (50 + Math.random() * 100)) {
					if (enemy.type != "enemy1") {
						shootBullet(enemy, enemy.target.p, true);
					} else if (enemy.down) {
						shootBullet(enemy, enemy.target.p, true);
					}

				} else if (enemy.click >= (400 + Math.random() * 400)) {
					enemy.click = 0;
					if (enemy.type == "eboss") {
						enemy.weapon = enemyWeapon[(int) (2 + Math.round(Math.random() * 2))];
					}
				}
				enemy.click++;
			});
		}
	}

	// enemy type 1 move event, dp for jump and only detect collision when up and
	// down is true
	private void move_type1(Entity enemy) {

		// update range, use jf as updateRange timer, initJF as range,
		if (enemy.jf % 100 == 0) {
			enemy.initJF = blockSize * ((Math.random() * 20) + 10);
		}
		enemy.jf++;
		enemy.left = enemy.p.getX() > player.p.getX();
		double range = Math.abs(player.p.getX() - enemy.p.getX());
		boolean boundary = range > blockCanvas.getWidth() / 2;
		// moving toward target
		if (boundary) {
			range = Math.abs(range - blockCanvas.getWidth());
			// reverse if reverse path is shorter
			enemy.left = !enemy.left;
		}
		// move toward target when range is outside of initjf
		if (range >= enemy.initJF) {
			enemy.up = true;
		}
		// moving away from target if it is 90% inside
		else if (range <= enemy.initJF * 0.9) {
			enemy.left = !enemy.left;
			enemy.up = true;
		} else
			enemy.up = false;

		// check left and right collision only when needed
		if (enemy.up && enemy.down) {
			double newX;
			if (enemy.left) {
				newX = enemy.p.getX() - enemy.speed / 2;
			} else {
				newX = enemy.p.getX() + enemy.speed / 2;
			}
			if (newX >= blockCanvas.getWidth()) {
				newX -= blockCanvas.getWidth();
			} else if (newX < 0) {
				newX += blockCanvas.getWidth();
			}
			// if there is new barrier set enemy x position to new x position
			if (!barrier_LeftRight(enemy, newX, enemy.left)) {
				enemy.moved = true;
				enemy.p.setX(newX);
			} else {
				enemy.dp = 20;
			}
		}

		double newY;
		// jump action
		if (enemy.dp > 0) {
			newY = enemy.p.getY() - enemy.speed;
			// check if there is a barrier on top
			if (!barrier_UpDown(enemy, newY - enemy.halfHeight, false)) {
				// enemy moved
				enemy.moved = true;
				// set new position
				enemy.p.setY(newY);
			} else {
				// shoot top block
				shootBullet(enemy, new Point(enemy.p.getX(), enemy.p.getY() - 10), false);
			}
			enemy.dp--;
		}
		// test if enemy will fall if enemy is not jumping and has moved
		else if (enemy.moved) {
			// falling speed
			newY = enemy.p.getY() + enemy.speed;
			if (!barrier_UpDown(enemy, (newY + enemy.halfHeight), true)) {
				enemy.p.setY(newY);

			} else {
				// stop jump
				enemy.dp = 0;
				enemy.moved = false;
				updateOccupied(enemy, enemy.halfWidth, enemy.halfHeight);
				// enemy is deployed on ground
				enemy.down = true;
			}
		} else
			enemy.dp = 0;

		// update block enemy occupied
		if (enemy.moved) {
			// modifyBoundary(enemy);
			updateOccupied(enemy, enemy.halfWidth, enemy.halfHeight);
		}

	}

	private void move_type2(Entity enemy) {
		// update range, use jf as updateRange timer, initJF as range,
		if (enemy.jf % 100 == 0) {
			enemy.initJF = blockSize * ((Math.random() * 20) + 10);
		}
		enemy.jf++;

		double range = Math.abs(player.p.getX() - enemy.p.getX());

		if (range > enemy.initJF) {

			enemy.left = enemy.p.getX() > player.p.getX();
		}

		boolean boundary = range > blockCanvas.getWidth() / 2;
		// moving toward target
		if (boundary) {
			range = Math.abs(range - blockCanvas.getWidth());
			// reverse if reverse path is shorter
			enemy.left = !enemy.left;
		}
		// move toward target when range is outside of initjf
		// if (range >= enemy.initJF) {
		// enemy.move = true;
		// }
		// moving away from target if it is 90% inside

		double heightrange = player.p.getY() - (blockSize * 5) - enemy.p.getY();
		// console.log(heightrange);
		if (heightrange < 0) {
			enemy.up = true;
		} else if (heightrange <= enemy.initJF) {
			if (enemy.initJF == 0) {
				enemy.up = rand.nextBoolean();
			}
		} else {
			enemy.up = false;
		}

		// check left and right collision only when needed
		// if (enemy.moved) {

		double newX = (enemy.left) ? enemy.p.getX() - enemy.speed : enemy.p.getX() + enemy.speed;
		if (newX >= blockCanvas.getWidth()) {
			newX -= blockCanvas.getWidth();
		} else if (newX < 0) {
			newX += blockCanvas.getWidth();
		}
		// if there is new barrier set enemy x position to new x position
		if (enemy.type == "eboss" || !barrier_LeftRight(enemy, newX, enemy.left)) {
			enemy.p.setX(newX);
		}

		// jump action
		double newY = (enemy.up) ? enemy.p.getY() - enemy.speed : enemy.p.getY() + enemy.speed;
		// check if there is a barrier on top
		if (enemy.type == "eboss"
				|| !barrier_UpDown(enemy, (enemy.up) ? newY - enemy.halfHeight : newY + enemy.halfHeight, !enemy.up)) {
			// set new position
			enemy.p.setY(newY);
		}
		updateOccupied(enemy, enemy.halfWidth, enemy.halfHeight);
		// }

	}

	/*----------------OBJECT EVENT----------------*/

	private void bulletMove() {
		if (!bulletList.isEmpty()) {
			Iterator<Bullet> it = bulletList.iterator();
			while (it.hasNext()) {
				Bullet bullet = it.next();
				// draw bullet
				// renderBullet(bullet);
				// get collided block array
				LinkedList<Block> blockArr = bullet.getCollideBlock();
				// do this if there is more than one collided block
				blockArr.forEach(block -> {
					// if it is a block
					if (block.stObj != null) {
						bulletAndObj(block, bullet);
					}
					// else it is a entity
					else {
						bulletAndEntity(block, bullet);
					}
				});
				// System.out.println(bullet.p.getX());
				if (bullet.hp > 0 && bullet.travelRange > 0) {
					// move bullet if travel range and hp is greater than 0
					bullet.move(blockCanvas);
					updateOccupied(bullet, bullet.radius, bullet.radius);
				} else {
					// remove this node if the bullet hp is 0 or collide with a block
					it.remove();
					bullet.destroy();
				}
			}
		}

	}

	// when bullet collide with stobj
	private void bulletAndObj(Block block, Bullet bullet) {
		// reduce block hp

		block.stObj.hp -= bullet.property.dmg;

		// clear block if block hp is 0 or less
		if (block.stObj.hp <= 0) {
			block.stObj = null;
			clearBlock(block.p);
			// add loot if there loot inside this block
			if (block.background.equals("loot")) {
				addLoot(block, 0);
			}
			player.moved = true;
		}
		// decrease bullet hp
		bullet.hp--;
	}

	// when bullet collide with entity
	private void bulletAndEntity(Block block, Bullet bullet) {
		Iterator<Obj> it = block.objs.iterator();
		while (it.hasNext()) {
			Obj o = it.next();
			// System.out.println(o.type+" hey");
			Entity obj = (Entity) o;
			// make sure it is not the shooter of the bullet
			if (obj.type.charAt(0) != bullet.owner.charAt(0) && testCollide(obj, bullet)) {
				boolean isPlayer = obj == player;
				// if energy shield active, obj is a player, and has over 200 energy.
				if (isPlayer && energyShield && energy >= 300) {
					// reduce energy instead of hp, but consume double dmg
					energy -= bullet.property.dmg * 4;
					if (energy <= 300) {
						energyShield = false;
					}
				} else {
					obj.hp -= bullet.property.dmg;
				}
				generateBlood(bullet.p);
				// destory obj if its hp is 0 or less and the object is a enemy
				if (obj.hp <= 0 && !isPlayer) {
					enemyList.remove(obj);

					obj.destroy();
					// add score
					score += 10;
					// loot amout
					double lootV = 25 + Math.round(Math.random() * 55);
					// bonus for defeating boss
					if (obj.type == "eboss") {
						lootV = 500;
						score += 90;
						player.hp += 150;
					}
					// drop loot
					addLoot(block, lootV);
				}
				// decrease bullet hp
				bullet.hp--;

			}
		}
	}

	// test collision
	private boolean testCollide(Entity obj, Bullet bullet) {
		// ballSize * (p1.radius) + ballSize * (p2.radius) - findDist(p1, p2) >= 0;
		return (obj.halfHeight + bullet.radius) - Math.abs(bullet.p.getY() - (obj.p.getY())) >= 0;
	}

	// add loot
	private void addLoot(Block block, double lootAmount) {
		// random loot amount from 10-90 if amout is undefined
		if (lootAmount <= 0) {
			lootAmount = Math.round(10 + Math.random() * 30);
		}
		// loot size based on loot amount
		double lootSize = (lootAmount / 60) * blockSize;
		// push loot inside list
		lootList.add(new Obj(block.p, lootSize, (int) lootAmount, Color.CYAN.brighter()));
		// clear this block's loot
		block.background = null;
	}

	// move loot
	private void lootMove() {
		if (!lootList.isEmpty()) {
			Iterator<Obj> it = lootList.iterator();
			while (it.hasNext()) {
				Obj loot = it.next();
				// check if player is within the range
				Iterator<Block> blockit = loot.blocks.iterator();
				while (blockit.hasNext()) {
					Block block = blockit.next();
					// increase pending energy and destroy self if player is within the range
					if (block.objs.contains(player)) {
						energy += loot.hp;
						it.remove();
						loot.destroy();
						return;
					}
				}

				Point velocity = PathCalculator.getVelocity(loot.p, player.p, movementSpeed * 2);

				// extreme case during boundary
				if (Math.abs(player.p.getX() - loot.p.getX()) > blockCanvas.getWidth() / 2) {
					loot.p.setX(loot.p.getX() - velocity.getX());
				} else {
					loot.p.setX(loot.p.getX() + velocity.getX());
				}
				modifyBoundary(loot);
				loot.p.setY(loot.p.getY() + velocity.getY());
				updateOccupied(loot, loot.radius, loot.radius);
			}
		}

	}

	private void modifyBoundary(Obj obj) {
		if (obj.p.getX() >= blockCanvas.getWidth()) {
			obj.p.setX(obj.p.getX() - blockCanvas.getWidth());
		} else if (obj.p.getX() < 0) {
			obj.p.setX(obj.p.getX() + blockCanvas.getWidth());
		}
	}

	private void bloodMove() {
		if (!bloodList.isEmpty()) {
			Iterator<Obj> it = bloodList.iterator();
			while (it.hasNext()) {

				Obj bld = it.next();
				if (bld.hp < 0) {
					it.remove();
					bld.destroy();
				} else {
					bld.p.setX(bld.p.getX() + bld.v.getX());
					bld.p.setY(bld.p.getY() + bld.v.getY());
					bld.hp -= 0.03;
				}
			}
		}
	}

	private void generateBlood(Point p) {
		double num = 2 + Math.random() * 5;
		for (int i = 0; i < num; i++) {
			double size = 3 + Math.random() * (blockSize / 4);
			Point v = new Point((2 - Math.random() * 4), (2 - Math.random() * 4));
			// p, diameter, hp, color
			Obj blood = new Obj(p, size, 1, Color.CYAN.darker(), "", v);
			bloodList.add(blood);
		}
	}

	/* ----------------CANVAS-DRAW private void---------------- */

	// generate new block canvas
	private void newBlockCanvas() {
		blockCanvas = new BufferedImage((int) (blockSize * mapSize.getX()), (int) (blockSize * mapSize.getY()),
				BufferedImage.TYPE_INT_RGB);

	}

	// render block
	private void renderBlock(Point p, Color c) {
		Graphics2D g2d = (Graphics2D) blockCanvas.getGraphics();
		g2d.setColor(c);
		g2d.fillRect((int) p.getX(), (int) p.getY(), blockSize, blockSize);
	}

	// clear block
	private void clearBlock(Point p) {
		Graphics2D g2d = (Graphics2D) blockCanvas.getGraphics();
		// if (obj.stObj == null) {
		g2d.clearRect((int) p.getX(), (int) p.getY(), blockSize, blockSize);
		/*
		 * } else { renderBlock(obj.p, obj.stObj.color); }
		 */
	}

	/*
	 * // render bullet private void renderBullet(Bullet bullet) { Graphics2D
	 * g2d=(Graphics2D)blockCanvas.getGraphics(); Point p = getXYRatio(bullet.p);
	 * g2d.setColor(Color.BLACK); g2d.fillOval((int)p.getX(), (int)p.getY(),
	 * (int)bullet.diameter, (int)bullet.diameter);
	 * 
	 * }
	 */

	// get x and y range based on player position
	private Point getXYRatio(Point p) {
		Point p2 = new Point(0, 0);
		if (Math.abs(p.getX() - player.p.getX()) > blockCanvas.getWidth() / 2) {
			if (p.getX() > player.p.getX()) {
				p2.setX(p.getX() - blockCanvas.getWidth() - (player.p.getX()) + board.getWidth() / 2);
			} else {
				p2.setX(blockCanvas.getWidth() + p.getX() - (player.p.getX()) + board.getWidth() / 2);
			}
		} else {
			p2.setX(p.getX() - (player.p.getX()) + board.getWidth() / 2);
		}
		p2.setY(p.getY() - (player.p.getY() + blockSize / 2) + board.getHeight() / 2);
		return p2;
	}

	private void renderObj(LinkedList<? extends Obj> objs, Graphics2D g2d) {
		if (!objs.isEmpty()) {
			objs.forEach(obj -> {
				g2d.setColor(obj.color);
				Point p = getXYRatio(obj.p);
				g2d.fillOval((int) p.getX(), (int) p.getY(), (int) obj.diameter, (int) obj.diameter);
			});
		}
	}

	// draw canvas
	private void draw(Graphics2D g2d) {

		drawBlockCanvas(g2d);
		renderObj(bloodList, g2d);
		renderObj(lootList, g2d);
		renderObj(bulletList, g2d);

		g2d.setFont(gameFont);

		g2d.setColor(player.color);
		g2d.fillRect((int) (board.getWidth() / 2 - (player.halfWidth)) + 1,
				(int) (board.getHeight() / 2 - (player.height * 0.75)), (int) player.width, (int) player.height);
		if (boss) {
			g2d.setColor(Color.BLUE);
		} else {
			g2d.setColor(Color.WHITE);
		}

		// draw loot if not empty
		if (!enemyList.isEmpty()) {
			enemyList.forEach(enemy -> {
				Point p = getXYRatio(enemy.p);
				g2d.fillRect((int) (p.getX() - enemy.halfWidth) + 1, (int) (p.getY() - enemy.halfHeight),
						(int) enemy.width, (int) enemy.height);
			});
		}

		g2d.setColor(Color.WHITE);

		// ctx.font = "20px Comic Sans MS";
		// ctx.textAlign = "start";
		g2d.drawString("FPS: " + Math.round(fps), 10, board.getHeight() - 10);

		g2d.drawString("Coordinate: (" + (int) player.p.getX() + "," + (int) player.p.getY() + ")",
				board.getWidth() / 2 - 80, board.getHeight() - 10);

		g2d.setColor(flyCanvasColor);
		int jf = (int) (player.jf * (board.getHeight() / player.initJF));
		g2d.fillRect(board.getWidth() - 10, board.getHeight() - jf, 20, jf);

		g2d.setColor(bldCanvasColor);
		g2d.fillRect(board.getWidth() - (int) player.hp, 0, (int) player.hp, 17);

		int eg = energy / 6;
		g2d.setColor(energyCanvasColor);
		g2d.fillRect(board.getWidth() - eg, 17, eg, 17);

		int textX = board.getWidth() - 80;
		g2d.setColor(Color.WHITE);
		g2d.drawString("HP: " + (int)player.hp, textX, 12);
		if (energyShield) {
			g2d.drawString("Energy (Shield): " + energy, textX - 80, 30);
		} else {
			g2d.drawString("Energy: " + energy, textX - 40, 30);
		}
		g2d.drawString("Jump Fuel: " + player.jf, textX - 50, board.getHeight() - 10);

		// ctx.font = "25px Comic Sans MS";

		g2d.drawString("Score: " + score, textX - 20, 60);
		g2d.drawString("Survive: " + surviveTime, textX - 30, 80);
		g2d.drawString("Enemies: " + enemyList.size(), textX - 30, 100);
		g2d.drawString("Tool: " + toolName[tool - 1], textX - 30, 120);

	}

	// draw sub image from block canvas
	private void drawBlockCanvas(Graphics2D g2d) {

		int centerX = board.getWidth() / 2;
		int centerY = board.getHeight() / 2;

		int sx = (int) (player.p.getX() + (player.halfWidth) - centerX);
		int sy = (int) (player.p.getY() + (player.halfHeight) - centerY);
		// connect ending point of block map to the starting point of the map when
		// player come to end boundary.
		if (sx + board.getWidth() > blockCanvas.getWidth()) {
			int vp = sx + board.getWidth();
			int w2 = vp - blockCanvas.getWidth();
			int w1 = board.getWidth() - w2;

			g2d.drawImage(blockCanvas.getSubimage(sx, sy, w1, board.getHeight()), 0, 0, w1, board.getHeight(), null);
			g2d.drawImage(blockCanvas.getSubimage(0, sy, w2, board.getHeight()), w1, 0, w2, board.getHeight(), null);
		}
		// connect starting point of block map to the ending point of the map when
		// player is over the starting boundary.
		else if (sx < 0) {
			int w2 = sx * -1;
			int w1 = board.getWidth() - w2;
			g2d.drawImage(blockCanvas.getSubimage(0, sy, w1, board.getHeight()), w2, 0, w1, board.getHeight(), null);
			g2d.drawImage(blockCanvas.getSubimage(blockCanvas.getWidth() - w2, sy, w2, board.getHeight()), 0, 0, w2,
					board.getHeight(), null);
		} else {
			g2d.drawImage(blockCanvas.getSubimage(sx, sy, board.getWidth(), board.getHeight()), 0, 0, board.getWidth(),
					board.getHeight(), null);
		}
	}

	private class Block {
		private Point p;
		private Obj stObj;
		private String background;
		private LinkedList<Obj> objs;

		private Block(Obj stObj, Point p, String background) {
			this(stObj, p);
			// background on the block (eg. flowers)
			this.background = background;
		}

		private Block(Obj stObj, Point p) {
			this.p = new Point(p.getX(), p.getY());
			// entity objs on the block (eg. player, enemy ...)
			this.objs = new LinkedList<Obj>();
			// static object on the block (eg. dirt, rock ...)
			this.stObj = stObj;
		}
	}

	// object
	protected class Obj {

		protected Point p;
		protected double diameter;
		protected double hp;
		protected Color color;
		protected double radius;
		protected LinkedList<Block> blocks;
		protected String type;
		protected Point v;

		protected Obj(double x, double y) {
			this.p = new Point(x, y);
		}

		protected Obj(Point p) {
			this.p = p;
		}

		protected Obj(Point p, double diameter, double hp, Color color) {
			// object color
			this.color = color;
			// object health
			this.hp = hp;
			// object diameter
			this.diameter = diameter;
			// object radius
			this.radius = diameter / 2;
			// object position
			this.p = new Point(p.getX(), p.getY());
			// block occupied
			this.blocks = new LinkedList<Block>();
		}

		protected Obj(Point p, double diameter, double hp, Color color, String type) {
			this(p, diameter, hp, color);
			this.type = type;
		}

		protected Obj(Point p, double diameter, double hp, Color color, String type, Point v) {
			this(p, diameter, hp, color, type);
			this.v = v;
		}

		protected void updateBlock(LinkedList<Block> newBlock) {
			// remove object reference in all occupied block
			this.destroy();

			// update new block data
			this.blocks = newBlock;

			// add object reference to new occupied block
			this.blocks.forEach(b -> {
				// if type is not undefined, then the object is a entity
				if (this.type != null && !this.type.isEmpty()) {
					// System.out.println("Hey: "+this.type);
					b.objs.add(Obj.this);
				}
			});
		}

		protected void destroy() {
			// check if the object is a entity
			boolean isEntity = (this.type != null && !this.type.isEmpty());
			if (isEntity) {
				// remove object reference in all occupied block
				this.blocks.forEach(b -> {
					// if (b.objs.contains(this)) {
					b.objs.remove(Obj.this);
					// }
				});
			}
		}
	}

	private class Bullet extends Obj {

		private Weapon property;
		private String owner;
		private double travelRange;

		// bullet extend of object
		private Bullet(Point p, Point v, Weapon property, String owner) {

			super(p, property.size, property.hp, property.color, "", v);
			// owner of the bullet
			this.owner = owner;
			// bullet property: size, speed, damage, and lifespan
			this.property = property;
			this.travelRange = property.travelRange;
		}

		// bullet move
		private void move(BufferedImage blockCanvas) {
			// update x and y by speed times velocity
			this.p.setX(this.p.getX() + this.v.getX());

			if (this.p.getX() >= blockCanvas.getWidth()) {
				this.p.setX(this.p.getX() - blockCanvas.getWidth());
			} else if (this.p.getX() < 0) {
				this.p.setX(this.p.getX() + blockCanvas.getWidth());
			}
			this.p.setY(this.p.getY() + this.v.getY());
			// reduce hp when travel
			this.travelRange--;
		};

		// return if collision block exist
		private LinkedList<Block> getCollideBlock() {
			// collided blocks
			LinkedList<Block> cblocks = new LinkedList<Block>();

			this.blocks.forEach(b -> {
				if (b.stObj != null || (!b.objs.isEmpty() && !b.objs.get(0).type.equals(this.owner))) {
					// push into collided blocks array if there is a obj in this block
					cblocks.add(b);
				}
			});
			// return collided blocks array
			return cblocks;
		};
	}

	private class Entity extends Obj {

		private double width, height, halfHeight, halfWidth, digpower, initJF, jf, dp, speed;
		private Weapon weapon;
		private Obj target;
		private int click;
		private boolean up, left, right, down, moved, fire;

		// player extend of object
		private Entity(Point p, double w, double h, int hp, double jumpFuel, double digpower, Weapon weapon, Obj target,
				double speed, String type, Color color) {

			// call super class constructor
			super(p, w, hp, color, type);
			// shoot at
			this.target = target;
			// weapon property: bullet size, bullet speed, damage, lifespan, cooldown, fire
			this.weapon = weapon;
			// -1 indicate there is no clicking
			this.click = -1;
			this.width = w;
			this.height = h;
			this.halfHeight = h / 2;
			this.halfWidth = w / 2;
			// player movement
			this.up = false;
			this.left = false;
			this.right = false;
			this.down = false;
			// player moved
			this.moved = true;
			// jump fuel
			this.initJF = jumpFuel;
			this.jf = jumpFuel;
			this.dp = digpower;
			this.speed = speed;
			this.fire = false;
		}

		private void recoverJump() {
			if (this.jf < 20) {
				this.jf = 20;
			}
			if (this.jf < this.initJF) {
				this.jf += 2;
			}
		};

		// count shooting cool down
		private void setCD() {
			this.fire = true;
			Helper.setTimeout(() -> {
				this.fire = false;
			}, this.weapon.cd);
			/*
			 * setTimeout(private void () { self.fire = false; },self.weapon.cd);
			 */
		};
	}

	private class Weapon {
		private double speed, size, dmg, travelRange, consume, hp;
		private int cd;
		private Color color;

		// weapon
		private Weapon(double speed, double size, double dmg, double travelRange, int cooldown, double consume,
				double hp, Color color) {
			this.speed = speed; // bullet travel speed
			this.size = size; // bullet size
			this.dmg = dmg; // damage doing
			this.travelRange = travelRange; // travel range
			this.cd = cooldown; // fire speed
			this.consume = consume; // energy consume
			this.hp = hp;
			this.color = color;
		}
	}

	private class Mouse extends Obj {

		private Mouse() {
			super(new Point(0, 0));
		}

		private Point mapXY() {
			return new Point((player.p.getX()) + this.p.getX() - board.getWidth() / 2,
					(player.p.getY() + blockSize / 2) + this.p.getY() - board.getHeight() / 2);
		}

	}
}
