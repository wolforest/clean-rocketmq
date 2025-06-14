package cn.coderule.minimq.broker.server.grpc.service.message;

import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.rpc.common.grpc.RequestPipeline;
import cn.coderule.minimq.rpc.common.grpc.activity.RejectActivity;
import cn.coderule.minimq.broker.server.grpc.activity.TransactionActivity;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.lang.exception.server.StartupException;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.broker.server.grpc.activity.ClientActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ConsumerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ProducerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.RouteActivity;
import cn.coderule.minimq.broker.server.bootstrap.BrokerContext;
import cn.coderule.minimq.domain.config.GrpcConfig;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class MessageManager implements Lifecycle {
    private final GrpcConfig grpcConfig;

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

    public MessageManager(GrpcConfig grpcConfig) {
        this.grpcConfig = grpcConfig;
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
        injectRouteController();
        injectProducerController();
        injectConsumerController();
        injectTransactionController();
    }

    @Override
    public void shutdown() {
        this.clientThreadPoolExecutor.shutdown();
        this.routeThreadPoolExecutor.shutdown();
        this.producerThreadPoolExecutor.shutdown();
        this.consumerThreadPoolExecutor.shutdown();
        this.transactionThreadPoolExecutor.shutdown();
    }

    private void initClientActivity() {
        this.clientThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getClientThreadNum(),
            grpcConfig.getClientThreadNum(),
            1,
            TimeUnit.MINUTES,
            "client-activity-thread-pool",
            grpcConfig.getClientQueueCapacity()
        );

        this.clientThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.clientActivity = new ClientActivity(
            this.clientThreadPoolExecutor
        );
    }

    private void initRouteActivity() {
        this.routeThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getRouteThreadNum(),
            grpcConfig.getRouteThreadNum(),
            1,
            TimeUnit.MINUTES,
            "route-activity-thread-pool",
            grpcConfig.getRouteQueueCapacity()
        );

        this.routeThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.routeActivity = new RouteActivity(
            this.grpcConfig,
            this.routeThreadPoolExecutor
        );
    }

    private void initProducerActivity() {
        this.producerThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getProducerThreadNum(),
            grpcConfig.getProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-activity-thread-pool",
            grpcConfig.getProducerQueueCapacity()
        );

        this.producerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.producerActivity = new ProducerActivity(
            this.producerThreadPoolExecutor
        );
    }

    private void initConsumerActivity() {
        this.consumerThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getConsumerThreadNum(),
            grpcConfig.getConsumerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "consumer-activity-thread-pool",
            grpcConfig.getConsumerQueueCapacity()
        );

        this.consumerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.consumerActivity = new ConsumerActivity(
            this.consumerThreadPoolExecutor
        );
    }

    private void initTransactionActivity() {
        this.transactionThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getTransactionThreadNum(),
            grpcConfig.getTransactionThreadNum(),
            1,
            TimeUnit.MINUTES,
            "transaction-activity-thread-pool",
            grpcConfig.getTransactionQueueCapacity()
        );

        this.transactionThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.transactionActivity = new TransactionActivity(
            this.transactionThreadPoolExecutor
        );
    }

    private void initMessageService() {
        RequestPipeline pipeline = (context, headers, request) -> {
        };

        pipeline = pipeline.pipe(new ContextInitPipeline());

        this.messageService = new MessageService(
            this.clientActivity,
            this.routeActivity,
            this.producerActivity,
            this.consumerActivity,
            this.transactionActivity,
            pipeline
        );
    }

    private void injectRouteController() {
        RouteController routeController = BrokerContext.getAPI(RouteController.class);
        if (routeController == null) {
            throw new StartupException("route controller is null");
        }
        this.routeActivity.setRouteController(routeController);
    }

    private void injectProducerController() {
        ProducerController producerController = BrokerContext.getAPI(ProducerController.class);
        if (producerController == null) {
            throw new StartupException("producer controller is null");
        }
        this.producerActivity.setProducerController(producerController);
    }

    private void injectConsumerController() {
        ConsumerController consumerController = BrokerContext.getAPI(ConsumerController.class);
        if (consumerController == null) {
            throw new StartupException("consumer controller is null");
        }
        this.consumerActivity.setConsumerController(consumerController);
    }

    private void injectTransactionController() {
        TransactionController transactionController = BrokerContext.getAPI(TransactionController.class);
        if (transactionController == null) {
            throw new StartupException("transaction controller is null");
        }
        this.transactionActivity.setTransactionController(transactionController);
    }
}
