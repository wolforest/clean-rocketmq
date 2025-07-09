package cn.coderule.minimq.store.server.ha;

import cn.coderule.minimq.store.server.ha.core.ConnectionState;

public interface HAClient {
    ConnectionState getConnectionState();
    void changeConnectionState(ConnectionState state);

    String getMasterAddress();
    String getMasterHaAddress();
    void setMasterAddress(String newAddress);
    void setMasterHaAddress(String newAddress);

    boolean connectMaster();
    void closeMaster();
}
