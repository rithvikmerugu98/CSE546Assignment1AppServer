package com.asu.cloudcomputing.awsclients;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.ArrayList;
import java.util.List;

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

    public List<Instance> getActiveAppInstances() {
        List<Instance> instances = new ArrayList<>();
        DescribeInstancesRequest req = DescribeInstancesRequest.builder()
                .filters(Filter.builder().name("instance-state-name")
                                .values("pending", "running").build(),
                        Filter.builder().name("tag:ServerType")
                                .values("AppTier").build()
                ).build();
        DescribeInstancesResponse res = ec2Client.describeInstances(req);
        if(res.hasReservations()) {
            for(Reservation reservation : res.reservations()) {
                if (reservation.hasInstances()) {
                    instances.addAll(reservation.instances());
                }
            }
        }
        return instances;
    }

    public static void main(String[] args) {
        Ec2AWSClient client = new Ec2AWSClient(Region.US_EAST_1);
        System.out.println(client.getActiveAppInstances());
    }

}
