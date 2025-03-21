package cn.coderule.minimq.registry.server.plugin;

import cn.coderule.minimq.rpc.common.RpcListener;
import io.netty.channel.Channel;

public class ConnectionManger implements RpcListener {
    @Override
    public void onConnect(String address, Channel channel) {

    }

    @Override
    public void onClose(String address, Channel channel) {

    }

    @Override
    public void onException(String address, Channel channel) {

    }

    @Override
    public void onIdle(String address, Channel channel) {

    }

    @Override
    public void onActive(String address, Channel channel) {

    }
}
