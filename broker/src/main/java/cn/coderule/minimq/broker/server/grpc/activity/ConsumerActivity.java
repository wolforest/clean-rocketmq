package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.GetOffsetRequest;
import apache.rocketmq.v2.GetOffsetResponse;
import apache.rocketmq.v2.QueryOffsetRequest;
import apache.rocketmq.v2.QueryOffsetResponse;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.UpdateOffsetRequest;
import apache.rocketmq.v2.UpdateOffsetResponse;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class ConsumerActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private ConsumerController consumerController;

    public ConsumerActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void receiveMessage(RequestContext context, ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
        ActivityHelper<ReceiveMessageRequest, ReceiveMessageResponse> helper = getReceiveHelper(context, request, responseObserver);

        try {
            Runnable task = () -> receiveMessageAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void ackMessage(RequestContext context, AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {
        ActivityHelper<AckMessageRequest, AckMessageResponse> helper = getAckHelper(context, request, responseObserver);

        try {
            Runnable task = () -> ackMessageAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void changeInvisibleDuration(RequestContext context,
        ChangeInvisibleDurationRequest request, StreamObserver<ChangeInvisibleDurationResponse> responseObserver) {
        ActivityHelper<ChangeInvisibleDurationRequest, ChangeInvisibleDurationResponse> helper = getInvisibleHelper(context, request, responseObserver);

        try {
            Runnable task = () -> changeInvisibleDurationAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void updateOffset(RequestContext context, UpdateOffsetRequest request, StreamObserver<UpdateOffsetResponse> responseObserver) {
        ActivityHelper<UpdateOffsetRequest, UpdateOffsetResponse> helper = getUpdateOffsetHelper(context, request, responseObserver);

        try {
            Runnable task = () -> updateOffsetAsync(context, request)
                .whenComplete(helper::writeResponse);
            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void getOffset(RequestContext context, GetOffsetRequest request, StreamObserver<GetOffsetResponse> responseObserver) {
        ActivityHelper<GetOffsetRequest, GetOffsetResponse> helper = getOffsetHelper(context, request, responseObserver);

        try {
            Runnable task = () -> getOffsetAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
    }

    public void queryOffset(RequestContext context, QueryOffsetRequest request, StreamObserver<QueryOffsetResponse> responseObserver) {
        ActivityHelper<QueryOffsetRequest, QueryOffsetResponse> helper = queryOffsetHelper(context, request, responseObserver);

        try {
            Runnable task = () -> queryOffsetAsync(context, request)
                .whenComplete(helper::writeResponse);

            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }

    }

    public CompletableFuture<ReceiveMessageResponse> receiveMessageAsync(RequestContext context, ReceiveMessageRequest request) {
        return null;
    }

    private ActivityHelper<ReceiveMessageRequest, ReceiveMessageResponse> getReceiveHelper(
        RequestContext context,
        ReceiveMessageRequest request,
        StreamObserver<ReceiveMessageResponse> responseObserver
    ) {
        Function<Status, ReceiveMessageResponse> statusToResponse = receiveStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, ReceiveMessageResponse> receiveStatusToResponse() {
        return status -> ReceiveMessageResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    public CompletableFuture<AckMessageResponse> ackMessageAsync(RequestContext context, AckMessageRequest request) {
        return null;
    }

    private ActivityHelper<AckMessageRequest, AckMessageResponse> getAckHelper(
        RequestContext context,
        AckMessageRequest request,
        StreamObserver<AckMessageResponse> responseObserver
    ) {
        Function<Status, AckMessageResponse> statusToResponse = ackStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, AckMessageResponse> ackStatusToResponse() {
        return status -> AckMessageResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    public CompletableFuture<ChangeInvisibleDurationResponse> changeInvisibleDurationAsync(RequestContext context, ChangeInvisibleDurationRequest request) {
        return null;
    }

    private ActivityHelper<ChangeInvisibleDurationRequest, ChangeInvisibleDurationResponse> getInvisibleHelper(
        RequestContext context,
        ChangeInvisibleDurationRequest request,
        StreamObserver<ChangeInvisibleDurationResponse> responseObserver
    ) {
        Function<Status, ChangeInvisibleDurationResponse> statusToResponse = invisibleStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, ChangeInvisibleDurationResponse> invisibleStatusToResponse() {
        return status -> ChangeInvisibleDurationResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    public CompletableFuture<UpdateOffsetResponse> updateOffsetAsync(RequestContext context, UpdateOffsetRequest request) {
        return null;
    }

    private ActivityHelper<UpdateOffsetRequest, UpdateOffsetResponse> getUpdateOffsetHelper(
        RequestContext context,
        UpdateOffsetRequest request,
        StreamObserver<UpdateOffsetResponse> responseObserver
    ) {
        Function<Status, UpdateOffsetResponse> statusToResponse = updateOffsetStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, UpdateOffsetResponse> updateOffsetStatusToResponse() {
        return status -> UpdateOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    public CompletableFuture<GetOffsetResponse> getOffsetAsync(RequestContext context, GetOffsetRequest request) {
        return null;
    }

    private ActivityHelper<GetOffsetRequest, GetOffsetResponse> getOffsetHelper(
        RequestContext context,
        GetOffsetRequest request,
        StreamObserver<GetOffsetResponse> responseObserver
    ) {
        Function<Status, GetOffsetResponse> statusToResponse = getOffsetStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, GetOffsetResponse> getOffsetStatusToResponse() {
        return status -> GetOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    public CompletableFuture<QueryOffsetResponse> queryOffsetAsync(RequestContext context, QueryOffsetRequest request) {
        return null;
    }

    private ActivityHelper<QueryOffsetRequest, QueryOffsetResponse> queryOffsetHelper(
        RequestContext context,
        QueryOffsetRequest request,
        StreamObserver<QueryOffsetResponse> responseObserver
    ) {
        Function<Status, QueryOffsetResponse> statusToResponse = queryOffsetStatusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

    private Function<Status, QueryOffsetResponse> queryOffsetStatusToResponse() {
        return status -> QueryOffsetResponse.newBuilder()
            .setStatus(status)
            .build();
    }

}
