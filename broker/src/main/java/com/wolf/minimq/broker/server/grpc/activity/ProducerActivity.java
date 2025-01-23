package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.Status;
import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ThreadPoolFactory;
import com.wolf.minimq.broker.api.ProducerController;
import com.wolf.minimq.broker.server.vo.RequestContext;
import com.wolf.minimq.domain.config.NetworkConfig;
import com.wolf.minimq.domain.model.bo.MessageBO;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ProducerActivity implements Lifecycle {
    private ThreadPoolExecutor executor;
    private final NetworkConfig networkConfig;
    private ProducerController producerController;

    public ProducerActivity(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    public void produce(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        Function<Status, SendMessageResponse> statusToResponse = statusToResponse();
        Runnable task = getTask(context, request, responseObserver);

        try {
            ActivityHelper.submit(context, request, responseObserver, executor, task, statusToResponse);
        } catch (Throwable t) {
            ActivityHelper.writeResponse(context, request, null, executor, t, responseObserver, statusToResponse);
        }
    }

    private Runnable getTask(RequestContext context, SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        return () -> {
            MessageBO messageBO = new MessageBO();
            producerController.produce(context, messageBO)
                .whenComplete((response, throwable) -> {
                    ActivityHelper.writeResponse(context, request, response, executor, throwable, responseObserver, statusToResponse());
                });
        };
    }

    private Function<Status, SendMessageResponse> statusToResponse() {
        return status -> SendMessageResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {
        executor = ThreadPoolFactory.create(
            networkConfig.getProducerThreadNum(),
            networkConfig.getProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-activity",
            networkConfig.getProducerQueueCapacity()
        );
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }
}
