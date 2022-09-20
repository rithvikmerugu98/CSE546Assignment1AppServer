package com.asu.cloudcomputing.classifier;

import com.asu.cloudcomputing.awsclients.AWSClientProvider;
import com.asu.cloudcomputing.awsclients.Ec2AWSClient;
import com.asu.cloudcomputing.awsclients.S3AWSClient;
import com.asu.cloudcomputing.awsclients.SQSAWSClient;
import com.asu.cloudcomputing.models.ImageDetail;
import com.asu.cloudcomputing.utility.PropertiesReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

public class ClassificationHandler {

    private static PropertiesReader props;
    private static AWSClientProvider awsClientsProvider;

    public ClassificationHandler() {
        try {
            props = new PropertiesReader("application.properties");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        awsClientsProvider = new AWSClientProvider();
    }

    public List<ImageDetail> saveImagesFromQueue() {
        SQSAWSClient sqsClient = awsClientsProvider.getSQSClient();
        String requestQueueURL = props.getProperty("amazon.sqs.request-queue");
        List<ImageDetail> response = sqsClient.saveImageFromQueue(requestQueueURL);
        System.out.println("Saved the images locally for " + response.stream().map(ImageDetail::getRequestId).collect(Collectors.joining(",")));
        return response;
    }


    public List<ImageDetail> classifyImages(List<ImageDetail> imageDetails) throws IOException {
        for(ImageDetail imageDetail : imageDetails) {

            System.out.println("Invoking the classification script for request - " + imageDetail.getRequestId());
            String command = "python3 ./image_classification.py ./CSE546Assignment1AppServer/" + imageDetail.getName();
            Process p = Runtime.getRuntime().exec(command, null, new File("../"));
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String scriptResult = in.readLine();
            System.out.println("Classified request " + imageDetail.getRequestId() + " as " + scriptResult);
            imageDetail.setClassifiedName(extractLabel(scriptResult));

        }
        return imageDetails;
    }

    private String extractLabel(String scriptResult) {
        return scriptResult.split(",")[1];
    }

    public void writeToResponseQueue(List<ImageDetail> imageDetails) {

        SQSAWSClient sqsClient = awsClientsProvider.getSQSClient();
        String responseQueueURL = props.getProperty("amazon.sqs.response-queue");
        for(ImageDetail details : imageDetails) {
            sqsClient.publishMessages(responseQueueURL, details.getClassifiedName(), details.getRequestId());
            System.out.println("Response of request " + details.getRequestId() + " saved to response queue");
        }

    }

    public void deleteFromRequestQueue(List<ImageDetail> imageDetails) {
        SQSAWSClient sqsClient = awsClientsProvider.getSQSClient();
        String requestQueueURL = props.getProperty("amazon.sqs.request-queue");
        System.out.println("Deleting processed messages from request Queue.");
        sqsClient.deleteMessages(requestQueueURL, imageDetails.stream().map(ImageDetail::getReceiptHandle).collect(Collectors.toList()));
    }

    public void saveToS3(List<ImageDetail> imageDetails) {
        for(ImageDetail details : imageDetails) {
            S3AWSClient s3Client = awsClientsProvider.getS3Client();
            String bucketName = props.getProperty("amazon.s3.bucket-name");
            System.out.println("Saved the image for " + details.getClassifiedName() + "in S3.");
            s3Client.uploadClassifiedImagesToS3(bucketName, details.getName(), details.getClassifiedName());
        }
    }

    public void deleteFilesLocally(List<ImageDetail> imageDetails) {
        for(ImageDetail imageDetail : imageDetails) {

            System.out.println("Deleting the local file - " + imageDetail.getName());
            File file = new File("./" + imageDetail.getName());
            file.delete();
        }
    }

    public void terminateInstance() {
        Ec2AWSClient ec2Client = awsClientsProvider.getEc2Client();
        int instances = ec2Client.getActiveAppInstances().size();
        if(instances > 2) {
            ec2Client.terminateInstance();
        }
    }
}
