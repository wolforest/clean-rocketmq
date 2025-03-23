package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.minimq.registry.domain.store.service.ChannelCloser;
import cn.coderule.minimq.rpc.common.RpcListener;
import io.netty.channel.Channel;

public class ConnectionCloser implements RpcListener {
    private final ChannelCloser channelCloser;

    public ConnectionCloser(ChannelCloser channelCloser) {
        this.channelCloser = channelCloser;
    }

    @Override
    public void onConnect(String address, Channel channel) {

    }

    @Override
    public void onClose(String address, Channel channel) {
        channelCloser.close(channel);
    }

    @Override
    public void onException(String address, Channel channel) {
        channelCloser.close(channel);
    }

    @Override
    public void onIdle(String address, Channel channel) {
        channelCloser.close(channel);
    }

    @Override
    public void onActive(String address, Channel channel) {

    }
}
