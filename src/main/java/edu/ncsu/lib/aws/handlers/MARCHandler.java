package edu.ncsu.lib.aws.handlers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;

import edu.ncsu.lib.marc.MarcTransformer;

/**
 * Sample event-driven AWS Lambda function that transforms MARC21 files uploaded to an S3 bucket into JSON then stores them into
 * a second S3 bucket.
 * @author adam_constabaris@ncsu.edu
 */
public class MARCHandler implements RequestHandler<S3Event, String> {

	private AmazonS3Client client = null;

	/**
	 * Main handler function.
	 * @param input an event that may contain one or more S3 upload notifications.
	 * @param context the Lambda context.
	 */
	@Override
	public String handleRequest(S3Event input, Context context) {
		AmazonS3Client client = getClient();

		/**
		 * sample transformation; this writes incoming MARC21 data to MARC-In-JSON format
		 */
		MarcTransformer transformer = new MarcTransformer();
		
		LambdaLogger logger = context.getLogger();
		
		List<PutObjectResult> results = new ArrayList<>();

		input.getRecords().forEach((action) -> {
			logger.log("Action: " + action.toString());
			String bucket = action.getS3().getBucket().getName();
			String s3Key = action.getS3().getObject().getKey();
			logger.log("Retrieving " + s3Key + " from bucket " + bucket);

			/**
			 * S3 PUT contents will be buffered unless we set the content-length in the metadata
			 * ahead of time.  We can't do that without storing the stream somewhere, so to reduce
			 * memory needs we will send the transformed output to a temporary file before uploading
			 * the result to the next bucket.
			 */
			File tempFile = null;
			try {
				tempFile = File.createTempFile("lambda-transform-", ".json");
			} catch (IOException iox) {
				logger.log("unable to create temporary file to buffer output:" + iox.getMessage());
				tempFile = null;
			}
			if (tempFile != null) {
				S3Object object = client.getObject(bucket, s3Key);
				object.getObjectMetadata().getContentLength();

				try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(tempFile))) {
					transformer.toJSON(object.getObjectContent(), output);
					output.flush();
					logger.log("Temporary file created.  Starting PUT to ingest-packages bucket");
					ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentType("application/json");
					PutObjectResult result = client.putObject(new PutObjectRequest("ingest-packages", s3Key, tempFile));
					results.add(result);
				} catch (IOException iox) {
					logger.log("Unable to transform input: " + iox.getMessage());
				}
			}

		});
		StringJoiner joiner = new StringJoiner(",");
		results.forEach((r) -> joiner.add(r.toString()));
		return joiner.toString();
	}

	protected AmazonS3Client getClient() {
		if (this.client == null) {
			this.client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain());
		}
		return this.client;
	}

	protected MARCHandler setClient(final AmazonS3Client client) {
		this.client = client;
		return this;
	}

}