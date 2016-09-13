package edu.ncsu.lib.marc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;

import org.marc4j.MarcJsonWriter;
import org.marc4j.MarcPermissiveStreamReader;
import org.marc4j.MarcReader;

import com.google.common.io.CountingInputStream;

import edu.ncsu.lib.io.IOMonitor;
import edu.ncsu.lib.io.MemoryMonitor;


/**
 * Sample transformer that converts incoming MARC21 content to MARC-In-JSON format.
 * @author adam_constabaris@ncsu.edu
 */
public class MarcTransformer {
	
	
	// an (optional) monitor that records the number of bytes read.
	private IOMonitor monitor;
	
	public IOMonitor monitor() {
		this.monitor = new IOMonitor();
		return monitor;
	}
	
	/**
	 * Convert an incoming stream of MARC21 data to MARC-In-JSON	
	 * @param input a stream of MARC21 encoded content.
	 * @param output a stream of MARC-In-JSON encoded content
	 * @return the number of records written to <code>output</code> (which should be equal to the number read from <code>input</code>).
	 * @throws IOException if an error is encountered reading or writing.
	 */
	public long toJSON(InputStream input, OutputStream output) throws IOException {
		final CountingInputStream counterInput = new CountingInputStream(input);
		long count = 0;
		if ( monitor != null ) {
			monitor.start();
		}
		try {
			MarcReader reader = new MarcPermissiveStreamReader(counterInput, true, true);
			MarcJsonWriter writer = new MarcJsonWriter(output, MarcJsonWriter.MARC_JSON);
			
			while( reader.hasNext() ) {
				if ( monitor != null ) {
					monitor.setRead(counterInput.getCount());
				}
				writer.write( reader.next() );
				count++;
			}
		} finally {
			if ( monitor != null ) {
				monitor.finish();
			}
			if ( input != null ) {
				try {
					input.close();
				} catch( IOException iox ) {
					
				}
			}
			if ( output != null ) {
				try {
					output.close();
				} catch( IOException iox ) {
					
				}
			}
		}
		return count;
	}
	
	
	public static void main(String[] args) {
		if ( args.length < 1 ) {
			System.err.println("Need an inputfilename");
			System.exit(1);
		}
		long count = -1;
		Timer t = new Timer();
		MemoryMonitor memMonitor = new MemoryMonitor();
		
		IOMonitor iOMonitor = null;
		t.schedule( memMonitor, 500L, 500L);
		try( FileInputStream input = new FileInputStream( args[0] ) ) {
			File destFile = args.length > 1 ? new File(args[1]) : File.createTempFile("marctransform",  ".json");
			if ( args.length > 1 ) {
				destFile.deleteOnExit();
			}
			
			try( OutputStream output = new FileOutputStream(destFile) ) {
				MarcTransformer transformer = new MarcTransformer();
				iOMonitor = transformer.monitor();				
				count = transformer.toJSON(input, output);	
			} 
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// stop recording memory use
			t.cancel();
		}
		
		long durationMS = iOMonitor.getDuration()/ 1000000;
		System.out.printf("Operation took %dms for %d records (%.4fms/record)%n", durationMS, count, (double)durationMS/(double)count);
		System.out.printf("Memory use: %d min, %d max%n", memMonitor.getMin(), memMonitor.getMax() );
		
	}

}
