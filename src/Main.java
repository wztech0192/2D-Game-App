import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/*
	**********************************
	File Name: Main.java
	Package: Default Package
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Contain Main Method
*/

public class Main{
	//Main Method
	public static void main(String[] args) {
		try{
    		for (LookAndFeelInfo info:UIManager.getInstalledLookAndFeels()){
        	if ("Nimbus".equals(info.getName())) {
        	    UIManager.setLookAndFeel(info.getClassName());
        	    break;
        		}
    		}
		} catch (Exception e) {}
    	SwingUtilities.invokeLater(new Runnable() {
            public void run(){
        		Interface f=new Interface();
        		f.setSize(800,650);
        		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        		f.setLocationRelativeTo(null);
        		f.setTitle("2D Ball App");       
        		f.setVisible(true);
        		f.init();
        	}
        });
	}

}
