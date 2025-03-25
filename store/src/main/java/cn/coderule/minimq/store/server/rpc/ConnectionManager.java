package cn.coderule.minimq.store.server.rpc;

import cn.coderule.minimq.rpc.common.RpcListener;
import io.netty.channel.Channel;

public class ConnectionManager implements RpcListener {
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
