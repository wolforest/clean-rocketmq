package cn.coderule.minimq.rpc.common.rpc.netty.handler;

import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.minimq.rpc.common.rpc.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.rpc.netty.service.NettyDispatcher;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcCommand> {
    private final NettyDispatcher dispatcher;

    public NettyServerHandler(NettyDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcCommand msg) {
        int localPort = NetworkUtil.getPort(ctx.channel().localAddress());
        if (-1 == localPort) {
            NettyHelper.close(ctx.channel());
            return;
        }

        RpcContext context = new RpcContext(ctx);
        this.dispatcher.dispatch(context, msg);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        changeAutoRead(ctx);
        super.channelWritabilityChanged(ctx);
    }

    private void changeAutoRead(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();

        if (!channel.isWritable()) {
            channel.config().setAutoRead(false);
            log.warn("Channel[{}] auto-read is disabled, bytes to drain before it turns writable: {}",
                NettyHelper.getRemoteAddr(channel), channel.bytesBeforeWritable());
            return;
        }

        if (channel.config().isAutoRead()) {
            return;
        }

        channel.config().setAutoRead(true);
        log.info("Channel[{}] turns writable, bytes to buffer before changing channel to un-writable: {}",
            NettyHelper.getRemoteAddr(channel), channel.bytesBeforeUnwritable());
    }
}

