package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import apache.rocketmq.v2.Status;
import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ThreadPoolFactory;
import com.wolf.minimq.broker.api.ProducerController;
import com.wolf.minimq.broker.server.RequestContext;
import com.wolf.minimq.domain.config.BrokerConfig;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class ProducerActivity implements Lifecycle {
    private ThreadPoolExecutor executor;
    private final BrokerConfig brokerConfig;
    private ProducerController producerController;

    public ProducerActivity(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
    }

    public void produce(SendMessageRequest request, StreamObserver<SendMessageResponse> responseObserver) {
        RequestContext context = RequestContext.create();
        Function<Status, SendMessageResponse> statusToResponse =
            status -> SendMessageResponse.newBuilder()
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
            brokerConfig.getProducerThreadNum(),
            brokerConfig.getProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-activity",
            brokerConfig.getProducerQueueCapacity()
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
