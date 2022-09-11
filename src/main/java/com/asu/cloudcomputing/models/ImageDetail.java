package com.asu.cloudcomputing.models;

public class ImageDetail {

    String name;
    String requestId;
    String receiptHandle;
    String classifiedName;

    public ImageDetail() {
    }


    public ImageDetail(String requestId, String receiptHandle) {
        this.name = requestId + ".jpg";
        this.requestId = requestId;
        this.receiptHandle = receiptHandle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public void setReceiptHandle(String receiptHandle) {
        this.receiptHandle = receiptHandle;
    }

    public String getClassifiedName() {
        return classifiedName;
    }

    public void setClassifiedName(String classifiedName) {
        this.classifiedName = classifiedName;
    }


    @Override
    public String toString() {
        return "ImageDetail{" +
                "name='" + name + '\'' +
                ", requestId='" + requestId + '\'' +
                ", receiptHandle='" + receiptHandle + '\'' +
                ", classifiedName='" + classifiedName + '\'' +
                '}';
    }
}
