package cn.coderule.minimq.rpc.broker.grpc.client;

import apache.rocketmq.v2.MessagingServiceGrpc;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.Resource;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("127.0.0.1", 8081)
            .usePlaintext()
            .build();

        MessagingServiceGrpc.MessagingServiceBlockingV2Stub stub
            = MessagingServiceGrpc.newBlockingV2Stub(channel).withDeadlineAfter(5000, TimeUnit.MILLISECONDS);

        QueryRouteRequest request = QueryRouteRequest.newBuilder()
            .setTopic(Resource.newBuilder().setName("MQT_29f5bcb8ac774f5a9e5bf34b98b51733"))
            .build();
        QueryRouteResponse response = stub.queryRoute(request);

        System.out.println(response);
        channel.shutdown();
    }
}
