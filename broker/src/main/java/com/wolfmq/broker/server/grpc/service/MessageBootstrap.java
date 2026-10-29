package com.wolfmq.broker.server.grpc.service;

import com.wolfmq.broker.api.RouteController;
import com.wolfmq.broker.server.grpc.GrpcMessageService;
import com.wolfmq.broker.server.grpc.interceptor.ContextInitPipeline;
import com.wolfmq.broker.server.grpc.interceptor.PipelineExecutor;
import com.wolfmq.broker.server.grpc.service.channel.ChannelManager;
import com.wolfmq.broker.server.grpc.service.channel.HeartbeatService;
import com.wolfmq.broker.server.grpc.service.channel.RegisterService;
import com.wolfmq.broker.server.grpc.service.channel.SettingManager;
import com.wolfmq.broker.server.grpc.service.channel.TelemetryService;
import com.wolfmq.broker.server.grpc.service.channel.TerminationService;
import com.wolfmq.broker.server.grpc.service.consume.GrpcAckService;
import com.wolfmq.broker.server.grpc.service.consume.InvisibleService;
import com.wolfmq.broker.server.grpc.service.consume.GrpcOffsetService;
import com.wolfmq.broker.server.grpc.service.consume.PopService;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.rpc.common.grpc.RequestPipeline;
import com.wolfmq.rpc.common.grpc.activity.RejectActivity;
import com.wolfmq.broker.server.grpc.activity.TransactionActivity;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.lang.exception.server.StartupException;
import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.broker.api.ProducerController;
import com.wolfmq.broker.api.TransactionController;
import com.wolfmq.broker.server.grpc.activity.ClientActivity;
import com.wolfmq.broker.server.grpc.activity.ConsumerActivity;
import com.wolfmq.broker.server.grpc.activity.ProducerActivity;
import com.wolfmq.broker.server.grpc.activity.RouteActivity;
import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.core.constant.ContextNamespace;
import com.wolfmq.domain.config.network.GrpcConfig;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class MessageBootstrap implements Lifecycle {
    private final ApplicationContext context;
    private final BrokerConfig brokerConfig;
    private final GrpcConfig grpcConfig;
    private final RejectActivity rejectActivity = new RejectActivity();

    @Getter
    private GrpcMessageService messageService;

    private ClientActivity clientActivity;
    private RouteActivity routeActivity;
    private ProducerActivity producerActivity;
    private ConsumerActivity consumerActivity;
    private TransactionActivity transactionActivity;

    private SettingManager settingManager;
    private ChannelManager channelManager;
    private RegisterService registerService;
    private HeartbeatService heartbeatService;
    private TelemetryService telemetryService;
    private TerminationService terminationService;
    private PipelineExecutor pipelineExecutor;

    protected ThreadPoolExecutor routeThreadPoolExecutor;
    protected ThreadPoolExecutor producerThreadPoolExecutor;
    protected ThreadPoolExecutor consumerThreadPoolExecutor;
    protected ThreadPoolExecutor clientThreadPoolExecutor;
    protected ThreadPoolExecutor transactionThreadPoolExecutor;

    public MessageBootstrap(ApplicationContext context, BrokerConfig brokerConfig) {
        this.context = context;
        this.brokerConfig = brokerConfig;
        this.grpcConfig = brokerConfig.getGrpcConfig();
    }

    @Override
    public void initialize() throws Exception {
        initPipelineExecutor();
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
            terminationService,
            pipelineExecutor
        );
    }

    private void initChannelService() {
        this.settingManager = new SettingManager(grpcConfig);
        this.channelManager = new ChannelManager(grpcConfig, settingManager);

        this.registerService = new RegisterService(channelManager);
        this.heartbeatService = new HeartbeatService(settingManager, registerService);
        this.telemetryService = new TelemetryService(settingManager, channelManager);
        this.terminationService = new TerminationService(settingManager, channelManager);
    }

    private void injectControllerToService() {
        this.settingManager.inject(context.getBean(ConsumerController.class, ContextNamespace.API));
        this.registerService.inject(
            context.getBean(RouteController.class, ContextNamespace.API),
            context.getBean(ProducerController.class, ContextNamespace.API),
            context.getBean(ConsumerController.class, ContextNamespace.API),
            context.getBean(TransactionController.class, ContextNamespace.API)
        );

        this.heartbeatService.inject(
            context.getBean(RouteController.class, ContextNamespace.API),
            context.getBean(ProducerController.class, ContextNamespace.API),
            context.getBean(ConsumerController.class, ContextNamespace.API),
            context.getBean(TransactionController.class, ContextNamespace.API)
        );

        this.telemetryService.inject(
            context.getBean(RouteController.class, ContextNamespace.API),
            context.getBean(ProducerController.class, ContextNamespace.API),
            context.getBean(ConsumerController.class, ContextNamespace.API),
            context.getBean(TransactionController.class, ContextNamespace.API)
        );

        this.terminationService.inject(
            context.getBean(ProducerController.class, ContextNamespace.API),
            context.getBean(ConsumerController.class, ContextNamespace.API)
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
            this.routeThreadPoolExecutor,
            this.pipelineExecutor
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
            this.producerThreadPoolExecutor,
            this.pipelineExecutor
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
            this.consumerThreadPoolExecutor,
            this.pipelineExecutor
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
            this.transactionThreadPoolExecutor,
            this.pipelineExecutor
        );
    }

    private void initPipelineExecutor() {
        RequestPipeline pipeline = (context, headers, request) -> {
        };

        pipeline = pipeline.pipe(new ContextInitPipeline());
        pipelineExecutor = new PipelineExecutor(pipeline);
    }

    private void initMessageService() {
        this.messageService = new GrpcMessageService(
            this.clientActivity,
            this.routeActivity,
            this.producerActivity,
            this.consumerActivity,
            this.transactionActivity
        );
    }

    private void injectRouteController() {
        RouteController routeController = context.getBean(RouteController.class, ContextNamespace.API);
        if (routeController == null) {
            throw new StartupException("route controller is null");
        }
        this.routeActivity.setRouteController(routeController);
    }

    private void injectProducerController() {
        ProducerController producerController = context.getBean(ProducerController.class, ContextNamespace.API);
        if (producerController == null) {
            throw new StartupException("producer controller is null");
        }
        this.producerActivity.setProducerController(producerController);
    }

    private void injectConsumerController() {
        ConsumerController consumerController = context.getBean(ConsumerController.class, ContextNamespace.API);
        if (consumerController == null) {
            throw new StartupException("consumer controller is null");
        }

        PopService popService = new PopService(
            brokerConfig,
            consumerController,
            settingManager
        );
        GrpcAckService ackService = new GrpcAckService(
            brokerConfig,
            consumerController
        );
        InvisibleService invisibleService = new InvisibleService(
            consumerController
        );
        GrpcOffsetService offsetService = new GrpcOffsetService();

        consumerActivity.inject(
            popService,
            ackService,
            invisibleService,
            offsetService
        );
    }

    private void injectTransactionController() {
        TransactionController transactionController = context.getBean(TransactionController.class, ContextNamespace.API);
        if (transactionController == null) {
            throw new StartupException("transaction controller is null");
        }

        TransactionService transactionService = new TransactionService(transactionController);
        this.transactionActivity.inject(transactionService);
    }
}
