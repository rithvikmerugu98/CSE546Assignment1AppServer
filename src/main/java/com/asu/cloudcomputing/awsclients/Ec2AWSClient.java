package com.asu.cloudcomputing.awsclients;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public class Ec2AWSClient {
    Ec2Client ec2Client;

    public Ec2AWSClient(Region region) {
        ec2Client = Ec2Client.builder().region(region).build();
    }

    public Ec2Client getEc2Client() {
        if(ec2Client == null) {
            ec2Client = Ec2Client.builder().build();
        }
        return ec2Client;
    }

    public void terminateInstance() {
        String instanceID = EC2MetadataUtils.getInstanceId();
        ec2Client.terminateInstances(TerminateInstancesRequest.builder().instanceIds(instanceID).build());
    }

}
