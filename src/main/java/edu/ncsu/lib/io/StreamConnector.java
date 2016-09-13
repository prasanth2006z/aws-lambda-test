package edu.ncsu.lib.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.google.common.io.ByteStreams;

import edu.ncsu.lib.marc.MarcTransformer;

/**
 * Utility for connecting input streams (data readers) through an intermedate transformation step.
 * <p>
 *  The primary use for this feature is when you wish to read data from one source, transform it, and read the output from the transform step  into another process, 
 *  without buffering the transformed result in memory or storing it to a temporary file.  The overall effect is similar to that of UNIX pipes: <code>process1 | process2</code>. 
 *  to how pipes work in UNIX/Linux:  
 * </p>
 * <p>
 *   This utility was originally developed to reduce memory usage for an AWS Lambda function that transforms large input streams (from S3 buckets) to large output streams (in different
 *   S3 buckets), but it turns out the S3 APIs buffer streams in memory in order to determine the amount of data.  As a result, the implementation had to be switched to use
 *   a temporary file.  Nonetheless, the technique may prove useful in other contexts.  Java provides a feature for doing this via <code>PipedInput/OutputStream</code> pairs objects,
 *   but their use requires that the reader and writer use different threads.  This class handles the piped stream setup and threading.
 *   </p> 
 * 
 * 
 */
public class StreamConnector {
	
	private ExecutorService executorService = Executors.newSingleThreadExecutor();
		
	/**
	 * Transforms an input stream using a supplied function to a new stream that can be read from, without using a large intermediate buffer.  Delegates to
	 * <code>transformStream(Supplier<InputStream>,BiFunction<InputStream,OutputStream,?>)</code>
	 * @param primaryInput the input from which the original data is read. 
	 * @param transformer a 2-argument function that transforms data read from <code>primaryInput</code> to an <code>OutputStream</code> (and which returns some value, e.g. the number of 
	 * bytes read)
	 * @return a stream of the transformed data from <code>primaryInput</code> and the <code>transformer</code> function.
	 * @throws IOException if an error is encountered reading or writing the streams.
	 * @see {@link #transformStream(Supplier, BiFunction)}
	 */
	public InputStream transformStream(final InputStream primaryInput, final BiFunction<InputStream,OutputStream, ?> transformer) throws IOException {
		return transformStream( () -> { return primaryInput; }, transformer); 
	}
	
	/**
	 * Transforms an input stream using a supplied function to a new stream that can be read from, without using a large intermediate buffer.
	 * 
	 * <p>
	 *  Uses a PipedInput/Output stream in a background thread
	 * </p>
	 * @param streamSupplier a function or method that returns an input strema to read the initial data from.  
	 * @param transformer a function or method that converts data read from an <code>InputStream</code> to an <code>OutputStream</code>.
	 * @return an InputStream over the transformed data.
	 * @throws IOException
	 */
	public InputStream transformStream(Supplier<InputStream> streamSupplier, BiFunction<InputStream,OutputStream, ?> transformer) throws IOException {		
		final PipedInputStream pipedInput = new PipedInputStream();
		final PipedOutputStream pipedOutput = new PipedOutputStream(pipedInput);
		final Callable<?> copier = () -> {
			return transformer.apply(streamSupplier.get(), pipedOutput);
			
		};
		executorService.submit( copier );
		return pipedInput;
	}
	
	public static void main(String[] args) throws Exception {
		MarcTransformer transformer = new MarcTransformer();
		InputStream input = new FileInputStream(args[0]);
		
		/**
		 * example : capture the output value of the transformer function
		 */
		CompletableFuture<Long> countPromise = new CompletableFuture<>();
		
		File output = File.createTempFile("marc-transorm-",  ".json");
		output.deleteOnExit();
		FileOutputStream outputStream = new FileOutputStream(output);
		
		long start = System.nanoTime();
		
		
		InputStream reader = new StreamConnector().transformStream(input, (t, u) -> {
			try {				
				long recordCount = transformer.toJSON(t, u);
				countPromise.complete(recordCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return -1L;
		});
		ByteStreams.copy( reader, outputStream );
		long count = countPromise.get();
		long end = System.nanoTime();
		long duration = end-start;
		
		long durationMS = duration / 1000000;
		
		System.out.printf("Operation took %dms for %d records (%.4fms/record)%n", durationMS, count, (double)durationMS/(double)count); 
		
		
	}

}
