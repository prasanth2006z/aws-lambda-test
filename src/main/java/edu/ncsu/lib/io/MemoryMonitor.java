package edu.ncsu.lib.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimerTask;

/**
 * Utility to log memory usage.
 * <p>
 * This is implemented as a <code>TimerTask</code> so it can be run periodically via a <code>Timer</code>.  It can be used outside that context by manually invoking
 * the <code>run()</code> method every time you wish to record a measurement.
 * </p>
 * <p>
 * Use of this utility should be limited; it provides a simple means of access to a very coarse measurement over time.  Production deployments should use a profiling or JMX-
 * based tool such as <code>jvisualvm</code> or <code>jmc</code>.
 * </p>
 * @author adam_constabaris
 */
public class MemoryMonitor extends TimerTask {
	
	
	private List<Long> measurements = new ArrayList<>();
	
	private long min = Long.MAX_VALUE;
	
	private long max = 0;
	
	private Runtime runtime = Runtime.getRuntime();
	
	public MemoryMonitor() {
		reset();
	}
	
	/**
	 * Records a memory usage measurement.  Sets <code>max</code> or <code>min</code> as appropriate.
	 */
	@Override
	public void run() {
		long total = runtime.totalMemory();
		this.measurements.add(total);
		if ( total < min ) {
			min = total; 
		}
		if ( total > max ) {
			max = total;
		}
	}
	
	/**
	 * Gets the lowest recorded measurement so far.
	 * @return
	 */
	public long getMin() {
		return min;
	}
	
	/**
	 * Get the highest recorded measurement so far.
	 * @return
	 */
	public long getMax() {
		return max;
	}
	
	/**
	 * Get all recorded measurements.
	 * @return
	 */
	public List<Long> getMeasurements() {
		return Collections.unmodifiableList(measurements);
	}
	
	/**
	 * Resets the monitor, clearing all measurements and resetting <code>min</code> and <code>max</code>
	 */
	public void reset() {
		this.measurements.clear();
		this.min = Long.MAX_VALUE;
		this.max = 0;
				
	}

}
