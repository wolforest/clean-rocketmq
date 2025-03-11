package cn.coderule.minimq.rpc.common.core.invoke;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

@Data
public class RpcContext {
    private ChannelHandlerContext channelContext;
    private String addr;

    public RpcContext(String addr) {
        this.addr = addr;
    }

    public RpcContext(ChannelHandlerContext channelContext) {
        this.channelContext = channelContext;
    }

    public Channel channel() {
        if (channelContext == null) {
            return null;
        }
        return channelContext.channel();
    }
}
