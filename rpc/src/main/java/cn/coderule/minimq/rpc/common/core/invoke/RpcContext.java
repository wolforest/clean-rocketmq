package cn.coderule.minimq.rpc.common.core.invoke;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public class RpcContext {
    private final ChannelHandlerContext channelContext;

    public RpcContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public Channel channel() {
        return channelContext.channel();
    }
}
