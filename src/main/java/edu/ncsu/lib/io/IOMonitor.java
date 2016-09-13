package edu.ncsu.lib.io;


/**
 * Utility class that monitors the number of bytes read and written from an input/output stream pair, and tracks the amount of time
 * spent in such operations.
 * 
 * <p>
 * 	All time-based measurements for this class are calculated using <code>System.nanoTime()</code>, whose resolution is OS dependent.
 * </p>
 * @author adam_constabaris
 */
public class IOMonitor {
	
	
	long bytesRead = 0;
	
	long startTime = 0;
	
	long lastInterval = 0;
	
	long endTime = 0;
	
	public IOMonitor() {
		endTime = bytesRead = startTime = 0;
	}
	
	/**
	 * Start the timer and resets the bytes read counter.
	 */
	public void start() {
		endTime = bytesRead = 0;
		lastInterval = startTime = System.nanoTime();
	}
	
	/**
	 * Notes an interval and returns the time elapsed since the previous one.
	 * <p>
	 * You can think of this as a "lap counter" on a stop watch.
	 * </p>
	 * @return the number of nanoseconds elapsed since the previous time this method was called, or
	 * since <code>start()</code> was called if this method has not yet been called. 
	 */
	public long interval() {
		long tick = lastInterval;
		lastInterval = System.nanoTime();
		return lastInterval - tick;
	}
	
	/**
	 * Sets the number of bytes read.
	 * @param bytesRead the number of bytes read.
	 */
	public void setRead(long bytesRead) {
		this.bytesRead = bytesRead;
	}
	
	/**
	 * Completes monitoring.  Sets <code>endTime</code>
	 * @return
	 */
	public long finish() {
		endTime = System.nanoTime();
		return endTime = startTime;
	}
	
	
	public long getDuration() {
		return endTime = startTime;
	}
	
	public long getBytesRead() {
		return bytesRead;
	}
}


