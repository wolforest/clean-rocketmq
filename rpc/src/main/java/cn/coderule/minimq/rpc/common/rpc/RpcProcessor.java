package cn.coderule.minimq.rpc.common.rpc;

import io.netty.channel.ChannelHandlerContext;

public interface RpcProcessor {
    RpcCommand process(ChannelHandlerContext ctx, RpcCommand request);
    boolean reject();
}
