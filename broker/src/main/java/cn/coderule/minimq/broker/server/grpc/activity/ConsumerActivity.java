package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.GetOffsetRequest;
import apache.rocketmq.v2.GetOffsetResponse;
import apache.rocketmq.v2.QueryOffsetRequest;
import apache.rocketmq.v2.QueryOffsetResponse;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.UpdateOffsetRequest;
import apache.rocketmq.v2.UpdateOffsetResponse;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Setter;

public class ConsumerActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private ConsumerController consumerController;

    public ConsumerActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void receiveMessage(RequestContext context, ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
    }

    public void ackMessage(RequestContext context, AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {
    }

    public void changeInvisibleDuration(RequestContext context,
        ChangeInvisibleDurationRequest request, StreamObserver<ChangeInvisibleDurationResponse> responseObserver) {
    }

    public void updateOffset(RequestContext context, UpdateOffsetRequest request, StreamObserver<UpdateOffsetResponse> responseObserver) {
    }

    public void getOffset(RequestContext context, GetOffsetRequest request, StreamObserver<GetOffsetResponse> responseObserver) {
    }

    public void queryOffset(RequestContext context, QueryOffsetRequest request, StreamObserver<QueryOffsetResponse> responseObserver) {
    }




}
