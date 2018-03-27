package com.swatt.util.general;

public class TimeProvider {
	
	private long creationTimeMillis;
	private long creationTimeNanos;
	
	public TimeProvider() {
		creationTimeMillis = System.currentTimeMillis();
		creationTimeNanos = System.nanoTime();
	}
	
	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}
	
	public long getCurrentTimeNanos() {
		return creationTimeMillis * 1000 + System.nanoTime() - creationTimeNanos;
	}

}
