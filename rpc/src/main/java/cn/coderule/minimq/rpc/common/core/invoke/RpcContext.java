package cn.coderule.minimq.rpc.common.core.invoke;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public class RpcContext {
    private ChannelHandlerContext channelContext;
}
