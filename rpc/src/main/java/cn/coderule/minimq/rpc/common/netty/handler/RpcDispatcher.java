package cn.coderule.minimq.rpc.common.netty.handler;

import cn.coderule.minimq.rpc.common.core.RpcCommand;
import cn.coderule.minimq.rpc.common.core.RpcProcessor;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ExecutorService;

public class RpcDispatcher {
    public void register(int requestCode, RpcProcessor processor, ExecutorService executor) {

    }

    public void dispatch(ChannelHandlerContext ctx, RpcCommand rpcCommand) {

    }
}
