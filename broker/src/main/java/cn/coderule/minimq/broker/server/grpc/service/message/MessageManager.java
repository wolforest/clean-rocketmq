package cn.coderule.minimq.broker.server.grpc.service.message;

import cn.coderule.minimq.broker.api.RouteController;
import cn.coderule.minimq.broker.server.grpc.service.ContextInitPipeline;
import cn.coderule.minimq.broker.server.grpc.service.channel.ChannelManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.HeartbeatService;
import cn.coderule.minimq.broker.server.grpc.service.channel.RegisterService;
import cn.coderule.minimq.broker.server.grpc.service.channel.SettingManager;
import cn.coderule.minimq.broker.server.grpc.service.channel.TelemetryService;
import cn.coderule.minimq.broker.server.grpc.service.channel.TerminationService;
import cn.coderule.minimq.broker.server.grpc.service.consume.AckService;
import cn.coderule.minimq.broker.server.grpc.service.consume.InvisibleService;
import cn.coderule.minimq.broker.server.grpc.service.consume.OffsetService;
import cn.coderule.minimq.broker.server.grpc.service.consume.PopService;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
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
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.rpc.common.grpc.core.GrpcRelayService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class MessageManager implements Lifecycle {
    private final BrokerConfig brokerConfig;
    private final GrpcConfig grpcConfig;

    @Getter
    private MessageService messageService;

    private ClientActivity clientActivity;
    private RouteActivity routeActivity;
    private ProducerActivity producerActivity;
    private ConsumerActivity consumerActivity;
    private TransactionActivity transactionActivity;
    private final RejectActivity rejectActivity = new RejectActivity();

    private RelayService relayService;
    private SettingManager settingManager;
    private ChannelManager channelManager;
    private RegisterService registerService;
    private HeartbeatService heartbeatService;
    private TelemetryService telemetryService;
    private TerminationService terminationService;

    protected ThreadPoolExecutor routeThreadPoolExecutor;
    protected ThreadPoolExecutor producerThreadPoolExecutor;
    protected ThreadPoolExecutor consumerThreadPoolExecutor;
    protected ThreadPoolExecutor clientThreadPoolExecutor;
    protected ThreadPoolExecutor transactionThreadPoolExecutor;

    public MessageManager(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        this.grpcConfig = brokerConfig.getGrpcConfig();
    }

    @Override
    public void initialize() throws Exception {
        initChannelService();

        initClientActivity();
        initRouteActivity();
        initProducerActivity();
        initConsumerActivity();
        initTransactionActivity();

        initMessageService();
    }

    @Override
    public void start() throws Exception {
        injectControllerToService();

        injectRouteController();
        injectProducerController();
        injectConsumerController();
        injectTransactionController();

        this.settingManager.start();
        this.channelManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.clientThreadPoolExecutor.shutdown();
        this.routeThreadPoolExecutor.shutdown();
        this.producerThreadPoolExecutor.shutdown();
        this.consumerThreadPoolExecutor.shutdown();
        this.transactionThreadPoolExecutor.shutdown();

        this.settingManager.shutdown();
        this.channelManager.shutdown();
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
            this.clientThreadPoolExecutor,
            heartbeatService,
            telemetryService,
            terminationService
        );
    }

    private void initChannelService() {
        this.settingManager = new SettingManager(grpcConfig);
        this.relayService = new GrpcRelayService();
        this.channelManager = new ChannelManager(grpcConfig, relayService, settingManager);

        this.registerService = new RegisterService(channelManager);
        this.heartbeatService = new HeartbeatService(settingManager, registerService);
        this.telemetryService = new TelemetryService(settingManager, channelManager, relayService);
        this.terminationService = new TerminationService(settingManager, channelManager);
    }

    private void injectControllerToService() {
        this.settingManager.inject(BrokerContext.getAPI(ConsumerController.class));
        this.registerService.inject(
            BrokerContext.getAPI(RouteController.class),
            BrokerContext.getAPI(ProducerController.class),
            BrokerContext.getAPI(ConsumerController.class),
            BrokerContext.getAPI(TransactionController.class)
        );

        this.heartbeatService.inject(
            BrokerContext.getAPI(RouteController.class),
            BrokerContext.getAPI(ProducerController.class),
            BrokerContext.getAPI(ConsumerController.class),
            BrokerContext.getAPI(TransactionController.class)
        );

        this.telemetryService.inject(
            BrokerContext.getAPI(RouteController.class),
            BrokerContext.getAPI(ProducerController.class),
            BrokerContext.getAPI(ConsumerController.class),
            BrokerContext.getAPI(TransactionController.class)
        );

        this.terminationService.inject(
            BrokerContext.getAPI(ProducerController.class),
            BrokerContext.getAPI(ConsumerController.class)
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

        PopService popService = new PopService(
            brokerConfig,
            consumerController,
            settingManager
        );
        AckService ackService = new AckService(
            consumerController,
            settingManager,
            channelManager
        );
        InvisibleService invisibleService = new InvisibleService(
            consumerController,
            settingManager,
            channelManager
        );
        OffsetService offsetService = new OffsetService();

        consumerActivity.inject(
            popService,
            ackService,
            invisibleService,
            offsetService
        );
    }

    private void injectTransactionController() {
        TransactionController transactionController = BrokerContext.getAPI(TransactionController.class);
        if (transactionController == null) {
            throw new StartupException("transaction controller is null");
        }
        this.transactionActivity.setTransactionController(transactionController);
    }
}
