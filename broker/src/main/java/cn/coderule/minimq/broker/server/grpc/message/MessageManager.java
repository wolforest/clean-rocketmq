package cn.coderule.minimq.broker.server.grpc.message;

import cn.coderule.minimq.broker.server.grpc.activity.RejectActivity;
import cn.coderule.minimq.broker.server.grpc.activity.TransactionActivity;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.ThreadPoolFactory;
import cn.coderule.common.lang.exception.server.StartupException;
import cn.coderule.minimq.broker.api.ConsumerController;
import cn.coderule.minimq.broker.api.ProducerController;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.broker.server.grpc.activity.ClientActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ConsumerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.ProducerActivity;
import cn.coderule.minimq.broker.server.grpc.activity.RouteActivity;
import cn.coderule.minimq.broker.server.context.BrokerContext;
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

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initClientActivity() {
        this.clientThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getGrpcClientThreadNum(),
            grpcConfig.getGrpcClientThreadNum(),
            1,
            TimeUnit.MINUTES,
            "client-activity-thread-pool",
            grpcConfig.getGrpcClientQueueCapacity()
        );

        this.clientThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.clientActivity = new ClientActivity(
            this.clientThreadPoolExecutor
        );
    }

    private void initRouteActivity() {
        this.routeThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getGrpcRouteThreadNum(),
            grpcConfig.getGrpcRouteThreadNum(),
            1,
            TimeUnit.MINUTES,
            "route-activity-thread-pool",
            grpcConfig.getGrpcRouteQueueCapacity()
        );

        this.routeThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.routeActivity = new RouteActivity(
            this.routeThreadPoolExecutor
        );
    }

    private void initProducerActivity() {
        this.producerThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getGrpcProducerThreadNum(),
            grpcConfig.getGrpcProducerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "producer-activity-thread-pool",
            grpcConfig.getGrpcProducerQueueCapacity()
        );

        this.producerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.producerActivity = new ProducerActivity(
            this.producerThreadPoolExecutor
        );
    }

    private void initConsumerActivity() {
        this.consumerThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getGrpcConsumerThreadNum(),
            grpcConfig.getGrpcConsumerThreadNum(),
            1,
            TimeUnit.MINUTES,
            "consumer-activity-thread-pool",
            grpcConfig.getGrpcConsumerQueueCapacity()
        );

        this.consumerThreadPoolExecutor.setRejectedExecutionHandler(rejectActivity);

        this.consumerActivity = new ConsumerActivity(
            this.consumerThreadPoolExecutor
        );
    }

    private void initTransactionActivity() {
        this.transactionThreadPoolExecutor = ThreadPoolFactory.create(
            grpcConfig.getGrpcTransactionThreadNum(),
            grpcConfig.getGrpcTransactionThreadNum(),
            1,
            TimeUnit.MINUTES,
            "transaction-activity-thread-pool",
            grpcConfig.getGrpcTransactionQueueCapacity()
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

    private void injectProducerController() {
        ProducerController producerController = BrokerContext.getAPI(ProducerController.class);
        if (producerController == null) {
            throw new StartupException("producer controller is null");
        }
        this.producerActivity.setProducerController(producerController);
        this.routeActivity.setProducerController(producerController);
    }

    private void injectConsumerController() {
        ConsumerController consumerController = BrokerContext.getAPI(ConsumerController.class);
        if (consumerController == null) {
            throw new StartupException("consumer controller is null");
        }
        this.consumerActivity.setConsumerController(consumerController);
        this.routeActivity.setConsumerController(consumerController);
    }

    private void injectTransactionController() {
        TransactionController transactionController = BrokerContext.getAPI(TransactionController.class);
        if (transactionController == null) {
            throw new StartupException("transaction controller is null");
        }
        this.transactionActivity.setTransactionController(transactionController);
    }
}
