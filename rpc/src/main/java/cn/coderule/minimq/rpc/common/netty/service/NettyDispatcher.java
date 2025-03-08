package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.RpcProcessor;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.ExecutorService;

public class NettyDispatcher {
    public void register(int requestCode, RpcProcessor processor, ExecutorService executor) {

    }

    public void dispatch(ChannelHandlerContext ctx, RpcCommand command) {
        if (command == null) {
            return;
        }

        switch (command.getType()) {
            case REQUEST_COMMAND:
                processRequest(ctx, command);
                break;
            case RESPONSE_COMMAND:
                processResponse(ctx, command);
                break;
            default:
                break;
        }
    }

    private void processRequest(ChannelHandlerContext ctx, RpcCommand command) {

    }

    private void processResponse(ChannelHandlerContext ctx, RpcCommand command) {

    }


}
