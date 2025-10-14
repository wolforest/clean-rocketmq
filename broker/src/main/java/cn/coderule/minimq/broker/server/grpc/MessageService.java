package cn.coderule.minimq.broker.server.grpc;

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
import cn.coderule.minimq.broker.server.grpc.activity.ClientActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ConsumerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ProducerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.RouteActivity;
import cn.coderule.minimq.broker.server.grpc.activity.TransactionActivity;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import com.google.protobuf.GeneratedMessage;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

/**
 * rRPC protocol implementation
 *
 * <ul>
 *   <li>execute pipeline</li>
 *   <li>execute related activity</li>
 * </ul>
 */
@Slf4j
public class MessageService extends MessagingServiceGrpc.MessagingServiceImplBase {
    private final ClientActivity clientActivity;
    private final ProducerActivity producerActivity;
    private final RouteActivity routeActivity;
    private final ConsumerActivity consumerActivity;
    private final TransactionActivity transactionActivity;

    private final RequestPipeline pipeline;

    public MessageService(
        ClientActivity clientActivity,
        RouteActivity routeActivity,
        ProducerActivity producerActivity,
        ConsumerActivity consumerActivity,
        TransactionActivity transactionActivity,
        RequestPipeline pipeline
    ) {

        this.clientActivity = clientActivity;
        this.routeActivity = routeActivity;
        this.producerActivity = producerActivity;
        this.consumerActivity = consumerActivity;
        this.transactionActivity = transactionActivity;
        this.pipeline = pipeline;
    }

    /**
     * route query api, producer/consumer will call this api while starting.
     *
     * @param request request
     * @param responseObserver gRPC response observer
     */
    @Override
    public void queryRoute(QueryRouteRequest request, StreamObserver<QueryRouteResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        routeActivity.getRoute(context, request, responseObserver);
    }

    /**
     * assignment query api, consumer will call this api while starting.
     *
     * @param request request
     * @param responseObserver gRPC response observer
     */
    @Override
    public void queryAssignment(QueryAssignmentRequest request, StreamObserver<QueryAssignmentResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        routeActivity.getAssignment(context, request, responseObserver);
    }

    @Override
    public void sendMessage(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        producerActivity.produce(context, request, responseObserver);
    }

    @Override
    public void receiveMessage(ReceiveMessageRequest request, StreamObserver<ReceiveMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.receiveMessage(context, request, responseObserver);
    }

    @Override
    public void ackMessage(AckMessageRequest request, StreamObserver<AckMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.ackMessage(context, request, responseObserver);
    }

    @Override
    public void changeInvisibleDuration(ChangeInvisibleDurationRequest request, StreamObserver<ChangeInvisibleDurationResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.changeInvisibleDuration(context, request, responseObserver);
    }

    @Override
    public void forwardMessageToDeadLetterQueue(
        ForwardMessageToDeadLetterQueueRequest request,
        StreamObserver<ForwardMessageToDeadLetterQueueResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        this.producerActivity.moveToDLQ(context, request, responseObserver);
    }

    @Override
    public void updateOffset(UpdateOffsetRequest request, StreamObserver<UpdateOffsetResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.updateOffset(context, request, responseObserver);
    }

    @Override
    public void getOffset(GetOffsetRequest request, StreamObserver<GetOffsetResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.getOffset(context, request, responseObserver);
    }

    @Override
    public void queryOffset(QueryOffsetRequest request, StreamObserver<QueryOffsetResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        consumerActivity.queryOffset(context, request, responseObserver);
    }

    @Override
    public void endTransaction(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        transactionActivity.submit(context, request, responseObserver);
    }

    /**
     * heartbeat api, register producer/consumer
     *
     * @param request request
     * @param responseObserver responseObserver
     */
    @Override
    public void heartbeat(HeartbeatRequest request, StreamObserver<HeartbeatResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        clientActivity.heartbeat(context, request, responseObserver);
    }

    @Override
    public void notifyClientTermination(
        NotifyClientTerminationRequest request, StreamObserver<NotifyClientTerminationResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        executePipeline(context, request);

        clientActivity.notifyClientTermination(context, request, responseObserver);
    }

    /**
     * telemetry API
     *
     * <ul>
     *   <li>register producer/consumer</li>
     *   <li>process trace</li>
     *   <li>verify message result</li>
     * </ul>
     */
    @Override
    public StreamObserver<TelemetryCommand> telemetry(StreamObserver<TelemetryCommand> responseObserver) {
        return clientActivity.telemetry(responseObserver, pipeline);
    }

    private <T> void executePipeline(RequestContext context, T request) {
        if (request instanceof GeneratedMessage) {
            pipeline.execute(context, GrpcConstants.METADATA.get(Context.current()), (GeneratedMessage) request);
            return;
        }

        log.error("[BUG] grpc request pipeline is not executed.");
    }

}
