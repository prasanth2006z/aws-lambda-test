# AWS Lambda Sample

This project contains a sample of an AWS Lambda function that performs a simple transfomration of MARC21-encoded files to MARC-In-JSON.

The purpose of this project is to provide a means of estimating the cost of using AWS services to perform "burst-style" processing in order to get a sense of the cost-effectiveness of the approach.

## How It Works

An AWS Lambda function is a single function call that can be invoked by a
number of different triggers.  When the trigger is fired, AWS instantiates a
server, sets up the environment, and invokes the function on that server, then
shuts down after the function call  is complete.  In principle, this allows users to pay for only the amount of processing they actually use.

In this partciular instance, the Lambda function
`edu.ncsu.lib.aws.handlers.MARCHandler.handleRequest` is designed to receive
notifications that files have been deposited in a particular Amazon S3 bucket,
transform them, and deposit the results into a different bucket.  This is
proof-of-concept code, and so for simplicity's sake the inputs are MARC21
encoded files and the outputs are those inputs converted to MARC-in-JSON
format.  To a certain extent, this mimics one part of the workflow of
maintaining a shared index:

- an institution uploads files in "Format X" 
- without further intervention from the uploader, processing is done on those files to transform the records into files that
  can be ingested by Solr.

A complete description of the setup in AWS for all of this is beyond the scope
of this README.

## Building

The only requirement on the build system is a Java 8 JDK.  To build, invoke
`./gradlew shadowJar` (or `./gradlew.bat shadowJar` on Windows); this will
download a suitable version of gradle and build the output.  The result will be
stored in
`build/libs/aws-lambda-test-all.jar` -- this is a complete jar file containing
all dependencies for the function, and can be uploaded directly to AWS via the
console.  I have not taken the time to set up uploading via the tools provided
by AWS.

N.B. As of initial commit, the unit tests do not successfully complete, but the
function has been run successfully on AWS multiple times.
