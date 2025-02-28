package cn.coderule.minimq.broker.server.grpc;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.ThreadPoolFactory;
import cn.coderule.common.lang.exception.SystemException;
import cn.coderule.minimq.broker.server.grpc.message.MessageService;
import cn.coderule.minimq.domain.config.GrpcConfig;
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
    public void initialize() {
        this.serverBuilder = NettyServerBuilder.forPort(config.getPort())
            .maxInboundMessageSize(config.getMaxInboundMessageSize())
            .maxConnectionIdle(config.getMaxConnectionIdle(), TimeUnit.MILLISECONDS)
            .addService(messageService)
            .addService(ChannelzService.newInstance(100))
            .addService(ProtoReflectionServiceV1.newInstance())
        ;

        initEventLoopGroup();

        this.server = serverBuilder.build();
    }

    @Override
    public void start() {
        try {
            this.server.start();
        } catch (Exception e) {
            log.error("start grpc server error", e);
            throw new SystemException("start grpc server error");
        }
    }

    @Override
    public void shutdown() {
        try {
            this.cleanup();
            this.businessThreadPool.shutdown();

            this.server.shutdown()
                .awaitTermination(config.getGrpcShutdownTimeout(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("shutdown grpc server error", e);
        }
    }

    @Override
    public void cleanup() {

    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private void initBusinessThreadPool() {
        this.businessThreadPool = ThreadPoolFactory.create(
            config.getGrpcBusinessThreadNum(),
            config.getGrpcBusinessThreadNum(),
            1,
            TimeUnit.MINUTES,
            "grpc-business-thread-pool",
            config.getGrpcBusinessQueueCapacity()
        );
    }

    private void initEventLoopGroup() {
        initBusinessThreadPool();

        if (config.isEnableGrpcEpoll()) {
            serverBuilder.channelType(EpollServerSocketChannel.class)
                .bossEventLoopGroup(new EpollEventLoopGroup(config.getGrpcBossThreadNum()))
                .workerEventLoopGroup(new EpollEventLoopGroup(config.getGrpcWorkerThreadNum()))
                .executor(businessThreadPool)
            ;

            return;
        }

        serverBuilder.channelType(NioServerSocketChannel.class)
            .bossEventLoopGroup(new NioEventLoopGroup(config.getGrpcBossThreadNum()))
            .workerEventLoopGroup(new NioEventLoopGroup(config.getGrpcWorkerThreadNum()))
            .executor(businessThreadPool)
        ;
    }

}
