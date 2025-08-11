package cn.coderule.minimq.broker.server.grpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.pool.ThreadPoolFactory;
import cn.coderule.common.lang.exception.SystemException;
import cn.coderule.minimq.broker.server.grpc.service.message.MessageService;
import cn.coderule.minimq.domain.config.network.GrpcConfig;
import cn.coderule.minimq.rpc.common.grpc.interceptor.ContextInterceptor;
import cn.coderule.minimq.rpc.common.grpc.interceptor.GlobalExceptionInterceptor;
import cn.coderule.minimq.rpc.common.grpc.interceptor.HeaderInterceptor;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.epoll.EpollServerSocketChannel;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.protobuf.services.ChannelzService;
import io.grpc.protobuf.services.ProtoReflectionServiceV1;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServer implements Lifecycle {
    private final GrpcConfig config;
    private final MessageService messageService;
    private Server server;

    private ThreadPoolExecutor businessThreadPool;

    private NettyServerBuilder serverBuilder;

    public GrpcServer(GrpcConfig config, MessageService messageService) {
        this.config = config;
        this.messageService = messageService;
    }

    @Override
    public void initialize() throws Exception {
        this.serverBuilder = NettyServerBuilder.forPort(config.getPort())
            .maxInboundMessageSize(config.getMaxInboundMessageSize())
            .maxConnectionIdle(config.getMaxConnectionIdle(), TimeUnit.MILLISECONDS)
            .addService(messageService)
            .addService(ChannelzService.newInstance(100))
            .addService(ProtoReflectionServiceV1.newInstance())
        ;

        initInterceptors();
        initEventLoopGroup();

        this.server = serverBuilder.build();
    }

    @Override
    public void start() throws Exception {
        try {
            this.server.start();
            log.info("start grpc server at port: {}", config.getPort());
        } catch (Exception e) {
            log.error("start grpc server error", e);
            throw new SystemException("start grpc server error");
        }
    }

    @Override
    public void shutdown() throws Exception {
        try {
            this.cleanup();
            this.businessThreadPool.shutdown();

            this.server.shutdown()
                .awaitTermination(config.getShutdownTimeout(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("shutdown grpc server error", e);
        }
    }

    private void initBusinessThreadPool() {
        this.businessThreadPool = ThreadPoolFactory.create(
            config.getBusinessThreadNum(),
            config.getBusinessThreadNum(),
            1,
            TimeUnit.MINUTES,
            "grpc-business-thread-pool",
            config.getBusinessQueueCapacity()
        );
    }

    private void initEventLoopGroup() {
        initBusinessThreadPool();

        if (config.isEnableEpoll()) {
            serverBuilder.channelType(EpollServerSocketChannel.class)
                .bossEventLoopGroup(new EpollEventLoopGroup(config.getBossThreadNum()))
                .workerEventLoopGroup(new EpollEventLoopGroup(config.getWorkerThreadNum()))
                .executor(businessThreadPool)
            ;

            return;
        }

        serverBuilder.channelType(NioServerSocketChannel.class)
            .bossEventLoopGroup(new NioEventLoopGroup(config.getBossThreadNum()))
            .workerEventLoopGroup(new NioEventLoopGroup(config.getWorkerThreadNum()))
            .executor(businessThreadPool)
        ;
    }

    private void initInterceptors() {
        this.serverBuilder
            .intercept(new GlobalExceptionInterceptor())
            .intercept(new ContextInterceptor())
            .intercept(new HeaderInterceptor())
            ;
    }


}
