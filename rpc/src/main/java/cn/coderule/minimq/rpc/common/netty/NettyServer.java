package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.common.lang.concurrent.DefaultThreadFactory;
import cn.coderule.common.util.lang.SystemUtil;
import cn.coderule.minimq.rpc.common.RpcServer;
import cn.coderule.minimq.rpc.common.core.RpcHook;
import cn.coderule.minimq.rpc.common.core.RpcListener;
import cn.coderule.minimq.rpc.common.core.RpcPipeline;
import cn.coderule.minimq.rpc.config.RpcServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import java.net.InetSocketAddress;

public class NettyServer extends NettyService implements RpcServer {
    private final RpcServerConfig config;
    private final RpcListener rpcListener;
    private final ServerBootstrap bootstrap;
    private final DefaultEventExecutorGroup eventExecutorGroup;

    public NettyServer(RpcServerConfig config) {
        this(config, null);
    }

    public NettyServer(RpcServerConfig config, RpcListener rpcListener) {
        super(config.getOnewaySemaphorePermits(), config.getAsyncSemaphorePermits());
        this.config = config;
        this.rpcListener = rpcListener;
        this.bootstrap = new ServerBootstrap();

        this.eventExecutorGroup = new DefaultEventExecutorGroup(
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
    public RpcListener getRpcListener() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    @Override
    public void registerRpcHook(RpcHook rpcHook) {

    }

    @Override
    public void clearRpcHook() {

    }

    @Override
    public void setRpcPipeline(RpcPipeline pipeline) {

    }

    private boolean useEpoll() {
        return SystemUtil.isLinux()
            && config.isUseEpoll()
            && Epoll.isAvailable();
    }
}
