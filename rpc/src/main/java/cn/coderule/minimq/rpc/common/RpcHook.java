package cn.coderule.minimq.rpc.common;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import io.netty.channel.ChannelHandlerContext;

public interface RpcHook {
    void beforeRequest(ChannelHandlerContext ctx, RpcCommand request);
    void afterResponse(ChannelHandlerContext ctx, RpcCommand request, RpcCommand response);
}
