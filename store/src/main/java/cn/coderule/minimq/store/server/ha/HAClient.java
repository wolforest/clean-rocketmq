package cn.coderule.minimq.store.server.ha;

import cn.coderule.minimq.store.server.ha.core.ConnectionState;

public interface HAClient {
    ConnectionState getConnectionState();
    void changeConnectionState(ConnectionState state);

    void updateMasterAddress(String newAddress);
    void updateMasterHaAddress(String newAddress);

    boolean connectMaster();
    void closeMaster();
}
