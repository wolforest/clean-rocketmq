package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.minimq.store.server.ha.HAClient;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultHAClient implements HAClient {
    @Override
    public ConnectionState getConnectionState() {
        return null;
    }

    @Override
    public void changeConnectionState(ConnectionState state) {

    }

    @Override
    public boolean connectMaster() {
        return false;
    }

    @Override
    public void closeMaster() {

    }
}
