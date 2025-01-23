package com.wolf.minimq.broker.server.grpc;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ThreadPoolFactory;
import com.wolf.minimq.broker.server.grpc.activity.ClientActivity;
import com.wolf.minimq.broker.server.grpc.activity.ConsumerActivity;
import com.wolf.minimq.broker.server.grpc.activity.ProducerActivity;
import com.wolf.minimq.broker.server.grpc.activity.RejectActivity;
import com.wolf.minimq.broker.server.grpc.activity.RouteActivity;
import com.wolf.minimq.broker.server.grpc.activity.TransactionActivity;
import com.wolf.minimq.domain.config.NetworkConfig;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class MessageManager implements Lifecycle {
    private final NetworkConfig networkConfig;

    @Getter
    private ClientActivity clientActivity;
    @Getter
    private RouteActivity routeActivity;
    @Getter
    private ProducerActivity producerActivity;
    @Getter
    private ConsumerActivity consumerActivity;
    @Getter
    private TransactionActivity transactionActivity;

    private final RejectActivity rejectActivity = new RejectActivity();

    @Getter
    private MessageService messageService;

    protected ThreadPoolExecutor routeThreadPoolExecutor;
    protected ThreadPoolExecutor producerThreadPoolExecutor;
    protected ThreadPoolExecutor consumerThreadPoolExecutor;
    protected ThreadPoolExecutor clientThreadPoolExecutor;
    protected ThreadPoolExecutor transactionThreadPoolExecutor;

    public MessageManager(NetworkConfig networkConfig) {
        this.networkConfig = networkConfig;
    }

    @Override
    public void initialize() {
        initClientActivity();
        initRouteActivity();
        initProducerActivity();
        initConsumerActivity();
        initTransactionActivity();

        initMessageService();
    }



    @Override
    public void start() {

    }

    @Override
    public void shutdown() {
        this.clientThreadPoolExecutor.shutdown();
        this.routeThreadPoolExecutor.shutdown();
        this.producerThreadPoolExecutor.shutdown();
        this.consumerThreadPoolExecutor.shutdown();
        this.transactionThreadPoolExecutor.shutdown();
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return null;
    }


    private void initClientActivity() {
        this.clientThreadPoolExecutor = ThreadPoolFactory.create(
            networkConfig.getClientThreadNum(),
            networkConfig.getClientThreadNum(),
            1,
            TimeUnit.MINUTES,
            "client-activity-thread-pool",
            networkConfig.getClientQueueCapacity()
        );

        this.clientThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.clientActivity = new ClientActivity(
            this.clientThreadPoolExecutor
        );
    }

    private void initRouteActivity() {
        this.routeThreadPoolExecutor = ThreadPoolFactory.create(
            networkConfig.getRouteThreadNum(),
            networkConfig.getRouteThreadNum(),
            1,
            TimeUnit.MINUTES,
            "route-activity-thread-pool",
            networkConfig.getRouteQueueCapacity()
        );

        this.routeThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.routeActivity = new RouteActivity(
            this.routeThreadPoolExecutor
        );
    }

    private void initProducerActivity() {
        this.producerThreadPoolExecutor = ThreadPoolFactory.create(
            networkConfig.getProducerThreadNum(),
            networkConfig.getProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-activity-thread-pool",
            networkConfig.getProducerQueueCapacity()
        );

        this.producerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.producerActivity = new ProducerActivity(
            this.producerThreadPoolExecutor
        );
    }

    private void initConsumerActivity() {
        this.consumerThreadPoolExecutor = ThreadPoolFactory.create(
            networkConfig.getConsumerThreadNum(),
            networkConfig.getConsumerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "consumer-activity-thread-pool",
            networkConfig.getConsumerQueueCapacity()
        );

        this.consumerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.consumerActivity = new ConsumerActivity(
            this.consumerThreadPoolExecutor
        );
    }

    private void initTransactionActivity() {
        this.transactionThreadPoolExecutor = ThreadPoolFactory.create(
            networkConfig.getTransactionThreadNum(),
            networkConfig.getTransactionThreadNum(),
            1,
            TimeUnit.MINUTES,
            "transaction-activity-thread-pool",
            networkConfig.getTransactionQueueCapacity()
        );

        this.transactionThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.transactionActivity = new TransactionActivity(
            this.transactionThreadPoolExecutor
        );
    }

    private void initMessageService() {
        this.messageService = new MessageService(
            this.clientActivity,
            this.routeActivity,
            this.producerActivity,
            this.consumerActivity,
            this.transactionActivity
        );
    }
}
