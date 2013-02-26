package com.groupagendas.groupagenda;

/**
 * A way to pass progress to the UI thread without reorganization.
 * 
 * @author Tadas
 */
public abstract class LoadProgressHook implements Runnable {
	public int cur;
	public int max;
	public int it;
	
	public void publish(int cur, int max) {
		this.cur = cur;
		this.max = max;
		run();
	}
	
	public void publish(int cur) {
		publish(cur, -1);
	}
	
	public LoadProgressHook resetIt() {
		it = 0;
		return this;
	}
	
	public final LoadProgressHook nextIt() {
		nextIteration();
		return this;
	}
	
	protected abstract void nextIteration();
}
