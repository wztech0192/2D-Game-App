package tool;

import java.awt.image.BufferedImage;

public class Animator implements Runnable {
	private BufferedImage[] image;
	private boolean run=true;
	private boolean animateLoop;
	private int start,end;
	private int index=-1;
	private int speed;
	private int defaultIndex;
	private boolean stop;
	private BufferedImage returnImage;
	
	public Animator(BufferedImage[] image, int defaultIndex, int speed) {
		this.image=image;
		this.index=defaultIndex;
		this.defaultIndex=defaultIndex;
		this.speed=speed;
		start=defaultIndex;
		end=image.length-1;
		returnImage=image[defaultIndex];
	}
	
	public void run() {
		while(run) {
			animating();
			try {
				Thread.sleep(speed);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}	
	
	public void animateStart() {
		run=true;
		Thread t=new Thread(this);
		t.start();
	}
	
	public void animateStop() {
		run=false;
	}
	
	public boolean isRunning() {
		return run;
	}
	
	public void animateSetting(int start, int end, boolean loop) {
		if(this.start!=start || this.end!=end) {
			this.start=start;
			index=start;
			this.end=end;
			animateLoop=loop;
			stop=false;
		}
	}
	
	public void setAnimateLoop(boolean animateLoop) {
		this.animateLoop=animateLoop;
	}
	
	private void animating() {
		if(stop) {
			returnImage=image[defaultIndex];
		}
		else {
			returnImage=image[index];
			if(!stop) {
				index++;
				if(index>end) {
					if(animateLoop) {
						index=start;
					}
					else {
						stop=true;
					}
				}
			}
		}
	}
	
	public void setDefault(int i) {
		defaultIndex=i;
	}
	
	public void defaultAnimate() {
		index=0;
		start=0;
		stop=true;
	}
	
	public BufferedImage getDefaultImage() {
		return image[defaultIndex];
	}
	
	public BufferedImage getImage() {
		return returnImage;
	}	
}
