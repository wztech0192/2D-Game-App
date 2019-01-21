/*
	**********************************
	File Name: Interface.java
	Package: Default Package
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Construct user interface 
	*Assign how each component react when active
	*Load and Save configuration data
*/

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import object.Board;
import tool.Tester;

final class Interface extends JFrame{
	private final GameConfig gameConfig=new GameConfig();
	private final SizeRangeConfig sizeConfig=new SizeRangeConfig();
	private Timer refresher;
	private final static String fileName="config/board_config";
	private final JCheckBox[] checkBox= {
			new JCheckBox("Set Pts Number:"),       //0
			new JCheckBox("Set Pts Limit:"),	 	//1
			new JCheckBox("Collision"),				//2
			new JCheckBox("Enclosing Circle"),		//3
			new JCheckBox("Enclosing Rectangle"),	//4
			new JCheckBox("Static Pts Size:"),		//5
			new JCheckBox("Animating"),				//6
			new JCheckBox("Auto Refresh"),			//7
			new JCheckBox("Show Grid"),				//8
			new JCheckBox("Use Background Image"),	//9
			new JCheckBox("Sync Color"),			//10
			new JCheckBox("Random R"),				//11
			new JCheckBox("Random G"),				//12
			new JCheckBox("Random B"),				//13
			new JCheckBox("Enable Blood Cencor")  	//14
	};
	
	private final SpinnerModel spinnerModel = new SpinnerNumberModel(0,0,1000000,1);
	private final SpinnerModel spinnerModel2 = new SpinnerNumberModel(50,50,2000,1);
	private final SpinnerModel spinnerModel3 = new SpinnerNumberModel(1,1,1000,1);
	private final JSpinner[] spinner= { new JSpinner(spinnerModel),new JSpinner(spinnerModel2),new JSpinner(spinnerModel3)};
	
	private final ButtonGroup bg=new ButtonGroup();
	private final JRadioButton[] radioBtn= {
			new JRadioButton("Add Point"),
			new JRadioButton("Control Point"),
			new JRadioButton("Draw Point"),
			new JRadioButton("Draw Wall"),
			new JRadioButton("Linear Detecting"),
			new JRadioButton("QuadTree Detecting")
	};
	
    private final Font font=new Font("Arial",Font.PLAIN,24);
    private final Font font2=new Font("Arial",Font.PLAIN,18);
	private final JButton btnRefresh=new JButton("Refresh");
	private final JButton btnBackColor=new JButton("Background Color");
	private final JButton btnPointColor=new JButton("Point Color");
	private final JButton btnIncrease=new JButton(">>>");
	private final JButton btnDecrease=new JButton("<<<");	
	
	private Color backColor;
	private Color pointColor;
	private double min,max;
	
	private final RBListener rbListener=new RBListener();
	private final MyTaskListener taskListener=new MyTaskListener();

	private Board board;
	
	
	//add component to Main Frame and assign the purpose of each component
	void init() {
		board=new Board(checkBox,radioBtn,spinner);
		
		bg.add(radioBtn[0]);
		bg.add(radioBtn[1]);
		bg.add(radioBtn[2]);
		bg.add(radioBtn[3]);
		final JButton btnExit=new JButton("EXIT");
		
		final ActionListener refreshListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
					board.refresh();
			}	
		};
		final CBListener cbListener=new CBListener();
		
		refresher=new Timer(1500,refreshListener);
		btnRefresh.addActionListener(refreshListener);
		btnExit.addActionListener(taskListener);
		btnIncrease.addActionListener(taskListener);
		btnDecrease.addActionListener(taskListener);
		btnPointColor.addActionListener(taskListener);
		btnBackColor.addActionListener(taskListener);
		
		checkBox[6].addActionListener(cbListener);
		checkBox[0].addActionListener(cbListener);
		checkBox[1].addActionListener(cbListener);
		checkBox[7].addActionListener(cbListener);
		checkBox[3].addActionListener(cbListener);
		checkBox[4].addActionListener(cbListener);
		checkBox[5].addActionListener(cbListener);
		checkBox[10].addActionListener(cbListener);
	
		btnRefresh.setBackground(Color.green);
		btnExit.setBackground(Color.red);
		btnExit.setForeground(Color.white);
		btnRefresh.setFont(font);
		btnExit.setFont(font);
		
		final JPanel mousePanel=new JPanel(new GridLayout(2,2,4,4));
		mousePanel.add(radioBtn[0]);
		mousePanel.add(radioBtn[1]);
		mousePanel.add(radioBtn[2]);
		mousePanel.add(radioBtn[3]);
		mousePanel.setBorder(new TitledBorder("Mouse Action"));
		
		final JPanel abilityPanel=new JPanel(new GridLayout(6,1));
		abilityPanel.add(checkBox[6]);
		abilityPanel.add(checkBox[2]);
		abilityPanel.add(checkBox[3]);
		abilityPanel.add(checkBox[4]);
		abilityPanel.add(checkBox[7]);
		abilityPanel.add(checkBox[8]);
		abilityPanel.setBorder(new TitledBorder(null,"Ability",TitledBorder.CENTER, TitledBorder.CENTER, font2));
		
		final JPanel colorPanel=new JPanel(new GridLayout(6,1));
		colorPanel.add(checkBox[10]);
		colorPanel.add(checkBox[11]);
		colorPanel.add(checkBox[12]);
		colorPanel.add(checkBox[13]);
		colorPanel.add(btnPointColor);	
		colorPanel.add(btnBackColor);
		colorPanel.setBorder(new TitledBorder(null,"Color",TitledBorder.CENTER, TitledBorder.CENTER, font2));
		
		final JPanel spnPanel=new JPanel(new GridLayout(3,3,4,4));
		spnPanel.add(checkBox[5]);
		spnPanel.add(spinner[2]);
		spnPanel.add(checkBox[0]);
		spnPanel.add(spinner[0]);
		spnPanel.add(checkBox[1]);
		spnPanel.add(spinner[1]);


		final JPanel ePanel=new JPanel(new GridLayout(1,2,4,4));
		ePanel.add(btnRefresh);
		ePanel.add(btnExit);
		
		final JPanel botPanel= new JPanel(new GridLayout(1,3,5,5));
		botPanel.add(mousePanel);
		botPanel.add(spnPanel);
		botPanel.add(ePanel);
		
		final JPanel leftPanel=new JPanel(new GridLayout(3,1));
		leftPanel.add(new JLabel(new ImageIcon(this.getClass().getResource("image/logo"+(1+(int)(Math.round(2*Math.random())))+".gif"))),BorderLayout.NORTH);
		leftPanel.add(abilityPanel,BorderLayout.CENTER);
		leftPanel.add(colorPanel,BorderLayout.SOUTH);
		
		
		board.setBackground(Color.white);
		
	/*	JScrollPane js=new JScrollPane(board);
		board.setPreferredSize(new Dimension(2500,2500));
		add(js,BorderLayout.CENTER);*/
		
		add(board,BorderLayout.CENTER);
		add(leftPanel,BorderLayout.WEST);
		add(botPanel, BorderLayout.SOUTH);
		radioBtn[0].doClick();
		
		//shortcut
		checkBox[6].setMnemonic(KeyEvent.VK_1);
		checkBox[2].setMnemonic(KeyEvent.VK_2);
		checkBox[3].setMnemonic(KeyEvent.VK_3);
		checkBox[4].setMnemonic(KeyEvent.VK_4);
		checkBox[7].setMnemonic(KeyEvent.VK_5);
		
		radioBtn[0].setMnemonic(KeyEvent.VK_A);
		radioBtn[1].setMnemonic(KeyEvent.VK_C);
		radioBtn[2].setMnemonic(KeyEvent.VK_D);
		radioBtn[3].setMnemonic(KeyEvent.VK_W);
		
		btnIncrease.setMnemonic(KeyEvent.VK_PERIOD);
		btnDecrease.setMnemonic(KeyEvent.VK_COMMA);
		checkBox[0].setMnemonic(KeyEvent.VK_Q);
		btnRefresh.setMnemonic(KeyEvent.VK_R);
		checkBox[1].setMnemonic(KeyEvent.VK_E);
	
		createMenuBar();
		loadSetting(fileName);
		//createCrusor();
	}
	
	//create crusor
	/*private void createCrusor() {
		final Image cursorImg =new ImageIcon(this.getClass().getResource("image/cursor.jpg")).getImage();
		// Create a new blank cursor.
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
				cursorImg, new Point(0, 0), "blank cursor");
		this.getContentPane().setCursor(cursor);
	}
	*/
	//Create menu bar interface
	private void createMenuBar() {
		final MenuListener menuListener=new MenuListener();
		final JMenuBar mainMenuBar=new JMenuBar();
		final JMenu menu1 = new JMenu("Tool");
		final JMenu menu2 = new JMenu("Help");
		final JMenu menu3 = new JMenu("Data");
		final JMenu menu4 = new JMenu("Collision");
		menu3.setMnemonic(KeyEvent.VK_F1);
		menu1.setMnemonic(KeyEvent.VK_F2);
		menu2.setMnemonic(KeyEvent.VK_F3);
		menu4.setMnemonic(KeyEvent.VK_F4);
		final JMenuItem menu1_item1 = new JMenuItem("Control Speed",KeyEvent.VK_D);
		final JMenuItem menu1_item2 = new JMenuItem("Random Size Range",KeyEvent.VK_B);
		final JMenuItem menu1_item3 = new JMenuItem("Timer Delay",KeyEvent.VK_T);
		final JMenuItem menu1_item4 = new JMenuItem("Refresher Delay",KeyEvent.VK_Y);
		final JMenuItem menu2_item1 = new JMenuItem("Shortcuts",KeyEvent.VK_H);
		final JMenuItem menu2_item2 = new JMenuItem("About",KeyEvent.VK_B);
		final JMenuItem menu3_item1 = new JMenuItem("Save Setting",KeyEvent.VK_0);
		final JMenuItem menu3_item2 = new JMenuItem("Store Default Setting",KeyEvent.VK_M);
		
		final JRadioButton menu4_item1 = new JRadioButton("Collision??");
		final JRadioButton menu4_item2 = new JRadioButton("Advanced Collision");
		final JRadioButton menu4_item3 = new JRadioButton("Ghost Collision");
		final JRadioButton menu4_item4 = new JRadioButton("Rotated Attraction");
		final ButtonGroup bg2=new ButtonGroup();
		final ButtonGroup bg3=new ButtonGroup();
		bg2.add(menu4_item1);
		bg2.add(menu4_item2);
		bg2.add(menu4_item3);
		bg2.add(menu4_item4);
		bg3.add(radioBtn[4]);
		bg3.add(radioBtn[5]);
		radioBtn[4].setSelected(true);
		menu4_item1.setSelected(true);

		
		menu1_item1.addActionListener(menuListener);
		menu1_item2.addActionListener(menuListener);
		menu1_item3.addActionListener(menuListener);
		menu1_item4.addActionListener(menuListener);
		menu2_item1.addActionListener(menuListener);
		menu2_item2.addActionListener(menuListener);
		menu3_item1.addActionListener(menuListener);
		menu3_item2.addActionListener(menuListener);
		menu4_item1.addItemListener(rbListener);
		menu4_item2.addItemListener(rbListener);
		menu4_item3.addItemListener(rbListener);
		menu4_item4.addItemListener(rbListener);

		menu1.add(menu1_item1);
		menu1.add(menu1_item2);
		menu1.add(menu1_item3);
		menu1.add(menu1_item4);
		menu2.add(menu2_item1);
		menu2.add(menu2_item2);
		menu3.add(menu3_item1);
		menu3.add(menu3_item2);
		menu4.add(radioBtn[4]);
		menu4.add(radioBtn[5]);
		menu4.addSeparator();
		menu4.add(menu4_item1);
		menu4.add(menu4_item2);
		menu4.add(menu4_item3);
		menu4.add(menu4_item4);
		
		
		final JMenu gameMenu = new JMenu("Game");
		addGameMenu("Dodge Ball","config/game_config/dodgeball/db_config",gameMenu,menuListener);
		addGameMenu("Shooting","config/game_config/shooting/st_config",gameMenu,menuListener);
		addGameMenu("Slash","config/game_config/slash/sl_config",gameMenu,menuListener);
		addGameMenu("Snake","config/game_config/snake/sk_config",gameMenu,menuListener);
		addGameMenu("Sword","config/game_config/sword/sw_config",gameMenu,menuListener);
		addGameMenu("Fractal Tree","config/game_config/fractaltree/ft_config",gameMenu,menuListener);
		addGameMenu("Minecraft 2D","config/game_config/mc2d/mc2d_config",gameMenu,menuListener);
		addGameMenu("SPBLXS","config/game_config/spblxs/spblxs_config",gameMenu,menuListener);
		
		gameMenu.addSeparator();
		gameMenu.add(checkBox[9]);
		gameMenu.add(checkBox[14]);
	
		mainMenuBar.add(menu3);
		mainMenuBar.add(menu1);
		mainMenuBar.add(menu2);
		mainMenuBar.add(menu4);
		mainMenuBar.add(gameMenu);
		mainMenuBar.add(Box.createHorizontalGlue());
		mainMenuBar.add(btnDecrease);
		btnDecrease.setFocusable(false);
		mainMenuBar.add(new JLabel("  Points Speed  "));
		mainMenuBar.add(btnIncrease);
		btnIncrease.setFocusable(false);
		
		setJMenuBar(mainMenuBar);
	}
	
	private void addGameMenu(String gameName, String fileURL, JMenu gameMenu, MenuListener menuListener) {
		final JMenu subMenu=new JMenu(gameName);
		final JMenuItem subMenu_start = new JMenuItem("Start");
		final JMenuItem subMenu_end= new JMenuItem("End");		
		subMenu_start.setActionCommand(gameName);
		final JMenuItem subMenu_config = new JMenuItem("Configuration");
		subMenu_config.setActionCommand(fileURL);
		final JMenuItem subMenu_guide = new JMenuItem("Game Guide");
		subMenu_guide.setActionCommand(gameName);
		
		subMenu_end.addActionListener(menuListener);
		subMenu_start.addActionListener(menuListener);
		subMenu_config.addActionListener(menuListener);
		subMenu_guide.addActionListener(menuListener);
		
		subMenu.add(subMenu_start);
		subMenu.add(subMenu_end);
		subMenu.add(subMenu_config);
		subMenu.add(subMenu_guide);
		
		gameMenu.add(subMenu);
		gameMenu.addSeparator();
	}
	

	//Set Collision method. Get selected JRadioButton from Collision Menu and send its command to board.
	private class RBListener implements ItemListener{
		@Override
		public void itemStateChanged(ItemEvent e){
			JRadioButton source=(JRadioButton)e.getSource();
			board.setCollision(source.getActionCommand());
		}
	}
	
	
	//Listener for JCheckBox
	private class CBListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JCheckBox source=(JCheckBox)(e.getSource());
			switch(source.getText()) {
				case "Set Pts Number:":
					if(source.isSelected()) 
						spinner[0].setEnabled(true);
					else
						spinner[0].setEnabled(false);
					break;
				case "Set Pts Limit:":
					if(source.isSelected()) 
						spinner[1].setEnabled(true);
					else
						spinner[1].setEnabled(false);
					break;
				case "Static Pts Size:":
					if(source.isSelected()) 
						spinner[2].setEnabled(true);
					else
						spinner[2].setEnabled(false);
					break;
				case "Animating":
					board.animateActive();
					break;
				case "Auto Refresh":
					if(source.isSelected()) 
						refresher.start();
					else 
						refresher.stop();
					break;
			}
		}
	}	


	//Listener for Menu
	private class MenuListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			JMenuItem source = (JMenuItem) (e.getSource());
			switch(source.getText()){
				case "Control Speed":
					String s=JOptionPane.showInputDialog(null,"*Current Control Speed: "+board.getControlSpeed()+"*\nSet Control Speed (Positive = Attract, Negative = Repel)");
					if(s!=null && Tester.isNumber(s)) {
						board.setControlSpeed(Double.parseDouble(s));
						board.repaint();
					}else JOptionPane.showMessageDialog(null,"Wrong Format - Input must be number");
					break;
				case "Random Size Range":
					sizeConfig.display();
					break;
				case "Timer Delay":
					try {
						int n;
						do {
							n=Integer.parseInt(JOptionPane.showInputDialog(null,"*Current Delay: "+board.getTimerDelay()+" MiliSecond*\nSet Timer Delay. (Must be greater than 0)"));
							if(n<1) {
								JOptionPane.showMessageDialog(null,"Input must be greater than 0");
							}
						}while(n<1);
						board.setTimerDelay(n);
						board.repaint();
					}
					catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "Must Be Numebr");
					}
					break;
				case "Refresher Delay":
					try {
						int n;
						do {
							n=Integer.parseInt(JOptionPane.showInputDialog(null,"*Current Delay: "+refresher.getDelay()+" MiliSecond*\nSet Refresher Delay. (Must be greater than 0)"));
							if(n<1) {
								JOptionPane.showMessageDialog(null,"Input must be greater than 0");
							}
						}while(n<1);
						refresher.setDelay(n);
					}
					catch(NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "Must Be Numebr");
					}
					break;
				case "Shortcuts":
					JOptionPane.showMessageDialog(null, "All Shortcuts:"
							+ "\n\n Point Move- ALT+1"
							+ "\n Collision- ALT+2"
							+ "\n Enclosing Circle- ALT+3"
							+ "\n Enclosing Rectange- ALT+4"
							+ "\n Random Point Size- ALT+5"
							+ "\n Auto Refresh- ALT+6"
							+ "\n\n Add Point - ALT+A"
							+ "\n Draw Point- ALT+D"
							+ "\n Control Point - ALT+C"
							+ "\n Draw Wall - ALT+W"
							+ "\n\n >>> - ALT+>"
							+ "\n <<< - ALT+<"
							+ "\n\n Set Point - ALT+Q"
							+ "\n Set Limit - ALT+E"
							+ "\n Refresh - ALT+R");
					break;
				case "Save Setting":
					saveSetting();
					break;
				case "Store Default Setting":
					int confirm = JOptionPane.showConfirmDialog(null,"ARE YOU CERTAIN YOU WANT TO DELETE THIS MENU?" ,"CONFIRM" ,JOptionPane.YES_NO_OPTION);
					if (confirm == JOptionPane.YES_OPTION){
						loadSetting(fileName+"_Default");
						btnRefresh.doClick();
					}
					break;
				case "About":
					JOptionPane.showMessageDialog(null, "A random game app written by Wei Zheng");
					break;
					
				case "Start":
					board.gameMode(source.getActionCommand());
					break;
				case "End":
					board.refresh();
					break;
				case "Configuration":
					gameConfig.setInfo(source.getActionCommand());
					gameConfig.display();
					board.refresh();
					break;
				case "Game Guide":
					gameConfig.showGuide(source.getActionCommand());
					break;
			}
		}
	}
	
	
	//Listener for some task
	private class MyTaskListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			switch(e.getActionCommand()) {
			
				case ">>>":
					board.increaseSpeed();
					break;
				
				case "<<<":
					board.decreaseSpeed();
					break;
				case "Background Color":
					 Color c = JColorChooser.showDialog(null, "Choose a Color for Background", backColor);
					 if(c!=null) {
						 backColor=c;
						 board.renderBackgroundColor(c);
					 }
					break;
				case "Point Color":
					Color c1 = JColorChooser.showDialog(null, "Choose a Color for Point", pointColor);
					if(c1!=null) {
						pointColor=c1;
						board.setDotColor(c1);
					}
					break;
				default: System.exit(0);
			}
			
		}
	}
	
	private class SizeRangeConfig extends JDialog{
		final JTextField[] input= {new JTextField(), new JTextField()};
		private SizeRangeConfig(){
			final ActionListener myListener =new ActionListener() {
				public void actionPerformed(ActionEvent e) { 
					switch(e.getActionCommand()) {
					case "Save":
						try {
							double minimum=Double.parseDouble(input[0].getText());
							double maximum=Double.parseDouble(input[1].getText());
							if(minimum>0 && maximum>minimum) {
								min=minimum;
								max=maximum;
								board.setRandomSizeRange(min, max);
								dispose();
							}else {
								JOptionPane.showMessageDialog(null,"Min<Max");
							}
						}
						catch(NumberFormatException ex) {
							JOptionPane.showMessageDialog(null,"Must Be Number");
						}
						break;
					case "Return":
						//undo changes and return to main screen
						dispose();
						break;
					}
				}
			};
			setLayout(new GridLayout(3,3));
			final JButton btnSave=new JButton("Save");
			final JButton btnCancel=new JButton("Return");
			btnSave.addActionListener(myListener);
			btnCancel.addActionListener(myListener);
			add(new JLabel("Min: "));
			add(input[0]);
			add(new JLabel("Max: "));
			add(input[1]);
			add(btnSave);
			add(btnCancel);
		}

		void display() {
			input[0].setText(min+"");
			input[1].setText(max+"");
			pack();
			setModal(true);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setLocationRelativeTo(null);   
			setVisible(true);
		}
	}
	

	/*Save the follow setting into config file
	 * All JCheckBox Selection, JRadioButton Selection, Point Size, Board Timer Delay, Board Control Speed, and Refresh Delay
	 */
	private void saveSetting() {
	
	  		try{  		
	  			BufferedWriter bw=new BufferedWriter(new FileWriter(fileName));
	  			
	  			for(JCheckBox cb:checkBox) {
	  				if(cb.isSelected())
	  					bw.write("T|");
	  				else bw.write("F|");
	  			}
	  			bw.newLine();		
	  			for(int i=0;i<radioBtn.length;++i) {
	  				if(radioBtn[i].isSelected()) {
	  					bw.write(i+"");
	  					break;
	  				}
	  			}
	  			bw.newLine();
	  			bw.write((int)spinner[0].getValue() + "|" +(int)spinner[1].getValue()+"|"+(int)spinner[2].getValue());
	  			bw.newLine();
	  			bw.write(refresher.getDelay()+"|"+board.getControlSpeed()+"|"+board.getTimerDelay()+"|"+min+"|"+max);
	    		bw.close();
	  		}catch(IOException e){
	  	 	e.printStackTrace ();
	  		}
	}
	

	//Read setting from config file
	private void loadSetting(String fileName) {
 		try{
   			BufferedReader br= new BufferedReader(new FileReader(fileName));
   			String[] in;
  			in=br.readLine().split("\\|");
  			for(int i=0;i<in.length;++i) {
  				if(in[i].equals("T")) {
  					checkBox[i].setSelected(true);
  				}else {
  					if(checkBox[i].isSelected()) {
  						checkBox[i].setSelected(false);;
  					}
  				}
  			}
			if(checkBox[0].isSelected()) 
				spinner[0].setEnabled(true);
			else
				spinner[0].setEnabled(false);
		
			if(checkBox[1].isSelected()) 
				spinner[1].setEnabled(true);
			else
				spinner[1].setEnabled(false);
			
			if(checkBox[5].isSelected()) 
				spinner[2].setEnabled(true);
			else
				spinner[2].setEnabled(false);
			
			if(checkBox[6].isSelected())
				board.animateActive();

			if(checkBox[7].isSelected()) 
				refresher.start();
			else 
				refresher.stop();

  			radioBtn[Integer.parseInt(br.readLine())].setSelected(true);
  			
  			in=br.readLine().split("\\|");
  			spinner[0].setValue(Integer.parseInt(in[0]));
  			spinner[1].setValue(Integer.parseInt(in[1]));
  			spinner[2].setValue(Integer.parseInt(in[2]));
  			
  			in=br.readLine().split("\\|");
  			refresher.setDelay(Integer.parseInt(in[0]));
  			board.setControlSpeed(Double.parseDouble(in[1]));
  			board.setTimerDelay(Integer.parseInt(in[2]));
  			min=Double.parseDouble(in[3]);
  			max=Double.parseDouble(in[4]);
  			board.setRandomSizeRange(min, max);
    		br.close();
  		}catch(IOException e){
   			e.printStackTrace ();
  		}
	}
}
