package cn.coderule.wolfmq.rpc.common.rpc.netty.handler;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcContext;
import cn.coderule.wolfmq.rpc.common.rpc.netty.service.helper.NettyDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcCommand> {
    private final NettyDispatcher dispatcher;

    public NettyClientHandler(NettyDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcCommand msg) throws Exception {
        RpcContext rpcContext = new RpcContext(ctx);
        this.dispatcher.dispatch(rpcContext, msg);
    }
}
