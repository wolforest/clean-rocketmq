package cn.coderule.minimq.rpc.common.rpc.netty.event;

import io.netty.channel.Channel;
import lombok.Getter;

@Getter
public class NettyEvent {
    private final NettyEventType type;
    private final String address;
    private final Channel channel;

    public NettyEvent(NettyEventType type, String address, Channel channel) {
        this.type = type;
        this.address = address;
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "NettyEvent [type=" + type + ", remoteAddr=" + address + ", channel=" + channel + "]";
    }
}
