package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.channel.ChannelHandlerContext;

public interface RpcProcessor {
    RpcCommand process(ChannelHandlerContext ctx, RpcCommand request);
    boolean reject();
}
