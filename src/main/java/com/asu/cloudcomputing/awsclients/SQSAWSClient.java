package com.asu.cloudcomputing.awsclients;

import com.asu.cloudcomputing.models.ImageDetail;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class SQSAWSClient {

    SqsClient sqsClient;

    public SQSAWSClient(Region region) {
        sqsClient = SqsClient.builder().region(region).build();
    }

    public List<ImageDetail> saveImageFromQueue(String queueURL) {
        List<ImageDetail> response = new ArrayList<>();
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueURL)
                .maxNumberOfMessages(10).build());
        if(receiveMessageResponse.hasMessages()) {
            List<Message> messages = receiveMessageResponse.messages();

            for(Message message : messages) {
                String name = message.messageAttributes().get("name").stringValue();
                String requestId = message.messageAttributes().get("requestId").stringValue();
                String receiptHandle = message.receiptHandle();
                ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getDecoder().decode(message.body()));
                try {
                    BufferedImage bImage2 = ImageIO.read(bis);
                    ImageIO.write(bImage2, "jpg", new File(name));
                } catch (IOException e) {
                    System.out.println("Unable to process image with request - " + name);
                    continue;
                }
                response.add(new ImageDetail(name, requestId, receiptHandle));
            }
        }
        return response;
    }

    public void publishMessages(String queueURL, String messageBody, String requestId, String name) {
        Map<String, MessageAttributeValue> attr = new HashMap<>();
        attr.put("requestId", MessageAttributeValue.builder().stringValue(requestId).build());
        attr.put("name", MessageAttributeValue.builder().stringValue(name).build());
        sqsClient.sendMessage(SendMessageRequest.builder()
                .messageBody(messageBody)
                .messageAttributes(attr)
                .queueUrl(queueURL).build());
    }


    public void deleteMessages(String queueUrl, List<String> messageReceipts){
        for(String receipt : messageReceipts) {

            DeleteMessageRequest request = DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(receipt)
                    .build();
           sqsClient.deleteMessage(request);
        }
    }

}
