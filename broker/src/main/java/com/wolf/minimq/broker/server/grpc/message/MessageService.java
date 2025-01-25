package com.wolf.minimq.broker.server.grpc.message;

import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResponse;
import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueRequest;
import apache.rocketmq.v2.ForwardMessageToDeadLetterQueueResponse;
import apache.rocketmq.v2.GetOffsetRequest;
import apache.rocketmq.v2.GetOffsetResponse;
import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import apache.rocketmq.v2.MessagingServiceGrpc;
import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import apache.rocketmq.v2.QueryAssignmentRequest;
import apache.rocketmq.v2.QueryAssignmentResponse;
import apache.rocketmq.v2.QueryOffsetRequest;
import apache.rocketmq.v2.QueryOffsetResponse;
import apache.rocketmq.v2.QueryRouteRequest;
import apache.rocketmq.v2.QueryRouteResponse;
import apache.rocketmq.v2.ReceiveMessageRequest;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.TelemetryCommand;
import apache.rocketmq.v2.UpdateOffsetRequest;
import apache.rocketmq.v2.UpdateOffsetResponse;
import com.wolf.minimq.broker.server.grpc.activity.ClientActivity;
import com.wolf.minimq.broker.server.grpc.activity.ConsumerActivity;
import com.wolf.minimq.broker.server.grpc.activity.ProducerActivity;
import com.wolf.minimq.broker.server.grpc.activity.RouteActivity;
import com.wolf.minimq.broker.server.grpc.activity.TransactionActivity;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageService extends MessagingServiceGrpc.MessagingServiceImplBase {
    private final ClientActivity clientActivity;
    private final ProducerActivity producerActivity;
    private final RouteActivity routeActivity;
    private final ConsumerActivity consumerActivity;
    private final TransactionActivity transactionActivity;

    public MessageService(
        ClientActivity clientActivity,
        RouteActivity routeActivity,
        ProducerActivity producerActivity,
        ConsumerActivity consumerActivity,
        TransactionActivity transactionActivity) {

        this.clientActivity = clientActivity;
        this.routeActivity = routeActivity;
        this.producerActivity = producerActivity;
        this.consumerActivity = consumerActivity;
        this.transactionActivity = transactionActivity;
    }

    @Override
    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        routeActivity.getRoute(request, responseObserver);
    }



    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        producerActivity.produce(request, responseObserver);
    }

    @Override
    public void queryAssignment(QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        routeActivity.getAssignment(request, responseObserver);
    }

    @Override
    public void receiveMessage(ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
        consumerActivity.receiveMessage(request, responseObserver);
    }

    @Override
    public void ackMessage(AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {
        consumerActivity.ackMessage(request, responseObserver);
    }

    @Override
    public void changeInvisibleDuration(
        ChangeInvisibleDurationRequest request, StreamObserver<ChangeInvisibleDurationResponse> responseObserver) {
        consumerActivity.changeInvisibleDuration(request, responseObserver);
    }

    @Override
    public void forwardMessageToDeadLetterQueue(
        ForwardMessageToDeadLetterQueueRequest request,
        StreamObserver<ForwardMessageToDeadLetterQueueResponse> responseObserver) {
        this.producerActivity.moveToDeadLetterQueue(request, responseObserver);
    }

    @Override
    public void updateOffset(UpdateOffsetRequest request, StreamObserver<UpdateOffsetResponse> responseObserver) {
        consumerActivity.updateOffset(request, responseObserver);
    }

    @Override
    public void getOffset(GetOffsetRequest request, StreamObserver<GetOffsetResponse> responseObserver) {
        consumerActivity.getOffset(request, responseObserver);
    }

    @Override
    public void queryOffset(QueryOffsetRequest request, StreamObserver<QueryOffsetResponse> responseObserver) {
        consumerActivity.queryOffset(request, responseObserver);
    }

    @Override
    public void endTransaction(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {
        transactionActivity.commit(request, responseObserver);
    }

    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        clientActivity.heartbeat(request, responseObserver);
    }

    @Override
    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return clientActivity.telemetry(responseObserver);
    }

    @Override
    public void notifyClientTermination(
        NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
        clientActivity.notifyClientTermination(request, responseObserver);
    }

}
