package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.minimq.rpc.common.RpcClient;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.codec.NettyDecoder;
import cn.coderule.minimq.rpc.common.netty.codec.NettyEncoder;
import cn.coderule.minimq.rpc.common.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.RpcListener;
import cn.coderule.minimq.rpc.common.netty.handler.ClientConnectionHandler;
import cn.coderule.minimq.rpc.common.netty.handler.NettyClientHandler;
import cn.coderule.minimq.rpc.common.netty.service.AddressInvoker;
import cn.coderule.minimq.rpc.common.netty.service.NettyService;
import cn.coderule.minimq.rpc.common.config.RpcClientConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyClient extends NettyService implements RpcClient {
    private final RpcClientConfig config;

    private final Bootstrap bootstrap;
    private final EventLoopGroup workerGroup;
    private final EventExecutorGroup businessGroup;
    private final NettyEventExecutor eventExecutor;

    private final AddressInvoker addressInvoker;

    public NettyClient(RpcClientConfig config) {
       this(config, null);
    }

    public NettyClient(RpcClientConfig config, RpcListener rpcListener) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits(), config.getCallbackThreadNum());
        this.config = config;

        this.bootstrap = new Bootstrap();
        this.eventExecutor = new NettyEventExecutor(rpcListener);
        this.workerGroup = buildWorkerGroup();
        this.businessGroup = buildEventExecutorGroup();

        initBootstrap();
        addressInvoker = new AddressInvoker(config, bootstrap, dispatcher, invoker);
    }

    @Override
    public void start() {
        eventExecutor.start();
        dispatcher.start();
    }

    private void shutdownCallbackExecutor() {
        if (callbackExecutor != null) {
            callbackExecutor.shutdown();
        }
    }

    @Override
    public void shutdown() {
        try {
            dispatcher.shutdown();

            workerGroup.shutdownGracefully();
            businessGroup.shutdownGracefully();
            eventExecutor.shutdown();

            shutdownCallbackExecutor();
        } catch (Exception e) {
            log.error("shutdown client error", e);
        }
    }

    @Override
    public Channel getOrCreateChannel(String addr) throws InterruptedException {
        return addressInvoker.getOrCreateChannel(addr);
    }

    @Override
    public ChannelFuture getOrCreateChannelAsync(String addr) throws InterruptedException {
        return addressInvoker.getOrCreateChannelAsync(addr);
    }

    @Override
    public RpcCommand invokeSync(String addr, RpcCommand request, long timeoutMillis) throws Exception {
        return addressInvoker.invokeSync(addr, request, timeoutMillis);
    }

    @Override
    public CompletableFuture<RpcCommand> invokeASync(String addr, RpcCommand request, long timeoutMillis) {
        return addressInvoker.invokeAsync(addr, request, timeoutMillis);
    }

    @Override
    public void invokeAsync(String addr, RpcCommand request, long timeoutMillis, RpcCallback invokeCallback) throws InterruptedException {
        addressInvoker.invokeAsync(addr, request, timeoutMillis, invokeCallback);
    }

    @Override
    public void invokeOneway(String addr, RpcCommand request, long timeoutMillis) throws Exception {
        addressInvoker.invokeOneway(addr, request, timeoutMillis);
    }

    @Override
    public boolean isChannelWritable(String addr) {
        return addressInvoker.isChannelWritable(addr);
    }

    @Override
    public boolean isAddressReachable(String addr) {
        return addressInvoker.isAddressReachable(addr);
    }

    @Override
    public void closeChannels(List<String> addrList) {
        addressInvoker.closeChannels(addrList);
    }

    @Override
    public void closeChannel(Channel channel) {
        addressInvoker.closeChannel(channel);
    }

    private void initBootstrap() {
        bootstrap.group(workerGroup)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, false)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeout())
            .handler(buildHandler())
        ;

        addCustomConfig();
    }

    private ChannelInitializer<SocketChannel> buildHandler() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(
                    businessGroup,
                    new NettyEncoder(),
                    new NettyDecoder(),
                    new IdleStateHandler(0, 0, config.getMaxChannelIdle()),
                    new ClientConnectionHandler(NettyClient.this, eventExecutor),
                    new NettyClientHandler(dispatcher)
                );
            }
        };
    }

    private void addCustomConfig() {
        if (config.getSendBufferSize() > 0) {
            log.info("client set SO_SNDBUF to {}", config.getSendBufferSize());
            bootstrap.option(ChannelOption.SO_SNDBUF, config.getSendBufferSize());
        }
        if (config.getReceiveBufferSize() > 0) {
            log.info("client set SO_RCVBUF to {}", config.getReceiveBufferSize());
            bootstrap.option(ChannelOption.SO_RCVBUF, config.getReceiveBufferSize());
        }
        if (config.getWriteBufferLowWater() > 0 && config.getWriteBufferHighWater() > 0) {
            log.info("client set netty WRITE_BUFFER_WATER_MARK to {},{}",
                config.getWriteBufferLowWater(), config.getWriteBufferHighWater());
            bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(
                config.getWriteBufferLowWater(), config.getWriteBufferHighWater()));
        }

        if (config.isEnableNettyPool()) {
            bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }
    }

    private EventLoopGroup buildWorkerGroup() {
        return new NioEventLoopGroup(
            config.getWorkerThreadNum(), new DefaultThreadFactory("NettyClientWorker_")
        );
    }

    private DefaultEventExecutorGroup buildEventExecutorGroup() {
        return new DefaultEventExecutorGroup(
            config.getBusinessThreadNum(), new DefaultThreadFactory("NettyClientBusiness_")
        );
    }
}
