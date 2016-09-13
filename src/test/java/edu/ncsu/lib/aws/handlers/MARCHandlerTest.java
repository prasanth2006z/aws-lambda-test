package edu.ncsu.lib.aws.handlers;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class MARCHandlerTest {

	@Test
	public void testHandler() {
		try {
			S3EventNotification notification = S3Event.parseJson( readResource("put-test.json") );
			S3Event event = new S3Event(notification.getRecords());
			
			MARCHandler handler = new MARCHandler();
			handler.setClient( getMockClient() );
			handler.handleRequest(event, getFakeContext());
			assertTrue("Looks like we made it", true);
		} catch( IOException iox ) {
			throw new RuntimeException("Unable to read test data from put-test.json", iox);
			
		}
		
	}
	
	private Context getFakeContext() {
		Context ctx = new Context() {

			@Override
			public String getAwsRequestId() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLogGroupName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getLogStreamName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getFunctionName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getFunctionVersion() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public String getInvokedFunctionArn() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public CognitoIdentity getIdentity() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public ClientContext getClientContext() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public int getRemainingTimeInMillis() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getMemoryLimitInMB() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public LambdaLogger getLogger() {
				return new LambdaLogger() {

					@Override
					public void log(String string) {
						System.out.println(string);
						
					}
					
				};
			}
			
		};
		return ctx;
	}
	
	
	String readResource(String name) throws IOException {
		StringBuilder sb = new StringBuilder();
		try(InputStream input = getClass().getResourceAsStream(name)) {
			BufferedReader br = new BufferedReader( new InputStreamReader(input, StandardCharsets.UTF_8 ));
			String line = null;
			while ( (line = br.readLine() ) != null ) {
				sb.append(line);
			}
		} 
		return sb.toString();
		
	}
	
	AmazonS3Client getMockClient() {
		S3Object mockObject = mock(S3Object.class);
		when(mockObject.getObjectContent()).thenReturn( mockStream( getClass().getResourceAsStream("/test.mrc") ) );
		AmazonS3Client result = mock(AmazonS3Client.class);
		when(result.getObject(anyString(), anyString())).thenReturn(mockObject);
		when(result.putObject(anyString(),anyString(),any(InputStream.class), any(ObjectMetadata.class))).thenReturn(mockPutResult());
		when(result.putObject(any(PutObjectRequest.class))).thenReturn(mockPutResult());
		return result;
				
	}
	
	private S3ObjectInputStream mockStream(final InputStream inputStream) {
		return new S3ObjectInputStream(inputStream, null); 
	}
	
	private PutObjectResult mockPutResult() {
		PutObjectResult result = mock(PutObjectResult.class);
		return result;
	}

}
