package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCallback;
import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.netty.event.RpcListener;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import cn.coderule.minimq.rpc.common.netty.service.NettyService;
import cn.coderule.minimq.rpc.common.config.RpcServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import lombok.Getter;

public class NettyServer extends NettyService implements RpcServer {
    private final RpcServerConfig config;
    @Getter
    private final RpcListener rpcListener;

    private final ServerBootstrap bootstrap;
    private final DefaultEventExecutorGroup eventExecutorGroup;

    public NettyServer(RpcServerConfig config) {
        this(config, null);
    }

    public NettyServer(RpcServerConfig config, RpcListener rpcListener) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits(), config.getCallbackThreadNum());
        this.config = config;
        this.rpcListener = rpcListener;
        this.bootstrap = new ServerBootstrap();

        this.eventExecutorGroup = buildEventExecutorGroup();
    }

    private DefaultEventExecutorGroup buildEventExecutorGroup() {
        return new DefaultEventExecutorGroup(
            config.getBusinessThreadNum(), new DefaultThreadFactory("NettyWorker_")
        );
    }

    private void initBootstrap() {
        bootstrap.group(buildBossGroup(), buildWorkerGroup())
            .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
            .option(ChannelOption.SO_BACKLOG, 1024)
            .option(ChannelOption.SO_REUSEADDR, true)
            .childOption(ChannelOption.SO_KEEPALIVE, false)
            .childOption(ChannelOption.TCP_NODELAY, true)
            .localAddress(new InetSocketAddress(config.getAddress(), config.getPort()))
            ;
    }

    private EventLoopGroup buildBossGroup() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(config.getBossThreadNum(), new DefaultThreadFactory("NettyEpollBoss_"));
        } else {
            return new NioEventLoopGroup(config.getBossThreadNum(), new DefaultThreadFactory("NettyNioBoss_"));
        }
    }

    private EventLoopGroup buildWorkerGroup() {
        if (useEpoll()) {
            return new EpollEventLoopGroup(config.getWorkerThreadNum(), new DefaultThreadFactory("NettyEpollBoss_"));
        } else {
            return new NioEventLoopGroup(config.getWorkerThreadNum(), new DefaultThreadFactory("NettyNioBoss_"));
        }
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    private boolean useEpoll() {
        return SystemUtil.isLinux()
            && config.isUseEpoll()
            && Epoll.isAvailable();
    }

    @Override
    public void registerProcessor(int requestCode, RpcProcessor processor, ExecutorService executor) {

    }

    @Override
    public RpcCommand invokeSync(Channel channel, RpcCommand request, long timeoutMillis) throws Exception {
        return null;
    }

    @Override
    public void invokeAsync(Channel channel, RpcCommand request, long timeoutMillis,
        RpcCallback callback) throws Exception {

    }

    @Override
    public void invokeOneway(Channel channel, RpcCommand request, long timeoutMillis) throws Exception {

    }
}
