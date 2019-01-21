/*
	**********************************
	File Name: GameConfig.java
	Package: Default Package

	Author: Wei Zheng
	**********************************

	Purpose:
	*Save and Edit Game Setting to Game Config File
*/


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

final class GameConfig extends JDialog{
	
	private String title;
	private String fileName;
	private String[] cbLabel;
	private JCheckBox[] checkBox;
	private Boolean[] cbBoolean;
	private boolean useCB;
	private String[] settingLabel;
	private String[] settingValue;
	private JTextField[] infoInput;
	private final GamePanel gamePanel=new GamePanel();
	
	
	//Contruct the user interface for config screen
	GameConfig(){	
		final ActionListener myListener =new ActionListener() {
			public void actionPerformed(ActionEvent e) { 
				switch(e.getActionCommand()) {
				case "Save Changes":
					//Proceed saving only if input mets the following requirement
					for(int i=0;i<infoInput.length;++i) {
						settingValue[i]=infoInput[i].getText();
						try {
							double value=Double.parseDouble(settingValue[i]);
							if(value<=0) {
								JOptionPane.showMessageDialog(null, "Input must greater or equal to 1");
								return;
							}
						}
						catch(NumberFormatException ex){
							JOptionPane.showMessageDialog(null, "Input must be a number");
							return;
						}
					}			
					saveSetting(fileName);
					dispose();
					break;
				case "Restore Default":
					//load default setting from config_defualt file
					loadSetting(fileName+"_Default");
					for(int i=0;i<infoInput.length;++i) {
						infoInput[i].setText(settingValue[i]);
					}
					if(useCB) {
						for(int i=0;i<cbBoolean.length;i++) {
							checkBox[i].setSelected(cbBoolean[i]);
						}
					}
					break;
				case "Cancel and Return":
					//undo changes and return to main screen
					dispose();
					break;
				}
			}
		};
		final JButton btnSave=new JButton("Save Changes");
		final JButton btnDefault=new JButton("Restore Default");
		final JButton btnCancel=new JButton("Cancel and Return");
		btnSave.addActionListener(myListener);
		btnDefault.addActionListener(myListener);
		btnCancel.addActionListener(myListener);
		final JPanel btnPanel=new JPanel();
		btnPanel.add(btnSave);
		btnPanel.add(btnDefault);
		btnPanel.add(btnCancel);		
		add(gamePanel,BorderLayout.CENTER);
		add(btnPanel,BorderLayout.SOUTH);
		
	}
	
	
	//set game config file
	void setInfo(String fileName) {
		this.fileName=fileName;
		loadSetting(fileName);
		gamePanel.setup();
		
	}
	
	
	//display config screen
	void display() {
		pack();
		setTitle(title);
		setModal(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);   
		setVisible(true);
	}
	
	
	/*Central part of config screen
	 * Components inside GamePanel will change base on the values inside config file
	 */
	private class GamePanel extends JPanel{		
		private void setup() {
			removeAll();
			int infoNum=settingLabel.length;
			if(useCB) {
				setLayout(new GridLayout(infoNum+cbLabel.length,2));
				checkBox=new JCheckBox[cbLabel.length];
				for(int i=0;i<cbLabel.length;++i) {
					add(new JLabel(cbLabel[i]));
					add(checkBox[i]=new JCheckBox("",cbBoolean[i]));
				}
			}else setLayout(new GridLayout(infoNum,2));
			infoInput=new JTextField[infoNum];
			for(int i=0;i<infoNum;++i) {
				add(new JLabel(settingLabel[i]));
				add(infoInput[i]=new JTextField(settingValue[i]));
			}
		}
	}
	
	
	//load setting description and value from game_config file
	private void loadSetting(String file) {
		try{
			BufferedReader br= new BufferedReader(new FileReader(file));
			String input[]=br.readLine().split("\\|");
			if(input.length>1) {
				useCB=true;
				cbBoolean=new Boolean[input.length/2];
				cbLabel=new String[input.length/2];
				for(int i=0;i<input.length;i+=2) {
					cbLabel[i/2]=input[i];
					if(input[i+1].equals("T")) {
						cbBoolean[i/2]=true;
					}else {
						cbBoolean[i/2]=false;
					}
				}
			}
			else useCB=false;
			settingValue=br.readLine().split("\\|");	
			settingLabel=br.readLine().split("\\|");
			title=br.readLine();
			br.close();
		}catch(IOException e){
			e.printStackTrace ();
		}
	}
	
	
	//set save value to game_config file
	private void saveSetting(String file) {
  		try{  		
  			BufferedWriter bw=new BufferedWriter(new FileWriter(file));
  			if(useCB) {
  				for(int i=0;i<checkBox.length;++i) {
	  				bw.write(cbLabel[i]+"|");
	  				if(checkBox[i].isSelected()) {
	  					bw.write("T|");
	  				}else bw.write("F|");
  				}
  			}
  			bw.newLine();
  			for(String i:settingValue) {
  				bw.write(i+"|");
  			}
  			bw.newLine();
  			for(String i:settingLabel) {
  				bw.write(i+"|");
  			}
  			bw.newLine();
  			bw.write(title);
    		bw.close();
  		}catch(IOException e){
  	 	e.printStackTrace ();
  		}
	}
	
	void showGuide(String i) {
		switch(i) {
		case "Dodge Ball":
			JOptionPane.showMessageDialog(null,"Dodge Ball:\n\n*Player control by mouse motion.\n*Score and level will increase as time goes by.\n*Game Over when player touches the ball");
			break;
		case "Shooting":
			JOptionPane.showMessageDialog(null,"Shooting:\n\n*Player control by mouse click and keyboard w,s,a,d.\n*Score will increase as player kills enermy.\n*Level will increase as all enermy is clear.\n*Game Over when enermy touches player\n*Boss will appear every 10 level");
			break;
		case "Slash":
			JOptionPane.showMessageDialog(null,"Slash Ninja:\n\n*Player control by mouse motion.\n*Score will increase as player hits the ball.\n*Level will increase as all enermy is clear.\n*Game Over when ball touches the bottom");
			break;
		case "Snake":
			JOptionPane.showMessageDialog(null,"Snake:\n\n*Player control by keyboard w,s,d,a.\n*Score and level will increase player eats point.\n*Game Over when player touches snake body");
			break;
		case "Sword":
			JOptionPane.showMessageDialog(null,"Sword:\n\n*Player control by keyboard w,s,d,a for Walk, Q,E for adjust shield angle.\n*Score and level will increase player eats point.\n*Game Over when player health become 0 \n*Boss will appear every 5 level");
			break;
		case "Fractal Tree":
			JOptionPane.showMessageDialog(null,"Fractal Tree:\n\n*Try to use the setting to make a pretty tree\n*Watch and enjoy");
			break;
		case "Minecraft 2D":
			JOptionPane.showMessageDialog(null,"Minecraft 2D:\n\n*2D Open World Sand Box Game");
			break;
		case "SPBLXS":
			JOptionPane.showMessageDialog(null, "Use W,A,D keys to move your character around. W key to jump up, the A key to move right, and the D key to move right.\r\n" + 
					"You can also press the spacebar to jump up. The can also dig down/destroy blocks with the mouse scroll button. The player can \r\n" + 
					"place blocks with the mouse right click.");
			break;
		
		}
	}
}
