package cn.coderule.minimq.rpc.common.core;

import io.netty.channel.Channel;

public interface RpcListener {
    void onConnect(final String address, final Channel channel);

    void onClose(final String address, final Channel channel);

    void onException(final String address, final Channel channel);

    void onIdle(final String address, final Channel channel);

    void onActive(final String address, final Channel channel);
}
