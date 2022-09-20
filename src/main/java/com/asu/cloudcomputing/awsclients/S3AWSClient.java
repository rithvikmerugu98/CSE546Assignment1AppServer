package com.asu.cloudcomputing.awsclients;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;


public class S3AWSClient {

    S3Client s3Client;

    public S3AWSClient(Region region) {
        s3Client = S3Client.builder().region(region).build();
    }

    public void uploadClassifiedImagesToS3(String bucketName, String filePath, String classifiedName) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("classifiedImages/" + classifiedName + ".jpeg")
                .build();
        PutObjectResponse res = s3Client.putObject(request, RequestBody.fromBytes(getObjectFile("./" + filePath)));
    }

    private static byte[] getObjectFile(String filePath) {

        FileInputStream fileInputStream = null;
        byte[] bytesArray = null;

        try {
            File file = new File(filePath);
            bytesArray = new byte[(int) file.length()];
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            try {
                fileInputStream.read(bytesArray);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bytesArray;
    }

}
