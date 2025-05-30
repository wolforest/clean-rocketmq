package cn.coderule.minimq.rpc.common.rpc.netty.handler;

import cn.coderule.minimq.rpc.common.rpc.netty.service.NettyHelper;
import cn.coderule.minimq.rpc.common.rpc.netty.event.NettyEvent;
import cn.coderule.minimq.rpc.common.rpc.netty.event.NettyEventExecutor;
import cn.coderule.minimq.rpc.common.rpc.netty.event.NettyEventType;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ChannelHandler.Sharable
public class ServerConnectionHandler extends ChannelDuplexHandler {
    private final NettyEventExecutor eventExecutor;

    public ServerConnectionHandler(NettyEventExecutor eventExecutor) {
        this.eventExecutor = eventExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelRegistered {}", remoteAddress);
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelUnregistered, the channel[{}]", remoteAddress);
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelActive, the channel[{}]", remoteAddress);
        super.channelActive(ctx);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.info("NETTY SERVER PIPELINE: channelInactive, the channel[{}]", remoteAddress);
        super.channelInactive(ctx);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent event)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        if (!event.state().equals(IdleState.ALL_IDLE)) {
            ctx.fireUserEventTriggered(evt);
            return;
        }

        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
        NettyHelper.close(ctx.channel());

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(
                new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel())
            );
        }

        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        final String remoteAddress = NettyHelper.getRemoteAddr(ctx.channel());
        log.warn("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
        log.warn("NETTY SERVER PIPELINE: exceptionCaught exception.", cause);

        if (eventExecutor.getRpcListener() != null) {
            eventExecutor.putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel()));
        }

        NettyHelper.close(ctx.channel());
    }
}

