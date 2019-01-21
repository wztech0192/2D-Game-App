/*
	**********************************
	File Name: RefreshRateTester.java
	Package: object
	
	Author: Wei Zheng
	**********************************

	Purpose:
	*Test timer refresh rate
*/
package tool;

public class RefreshRateTester {
	private long rate;
	private long before;
	
	public void test() {
		long now=System.currentTimeMillis();
		rate=now-before;
		before=now;
	}
	
	public long getRate() {
		return rate;
	}
}
