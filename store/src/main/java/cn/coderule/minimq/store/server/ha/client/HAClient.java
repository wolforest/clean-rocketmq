package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.minimq.store.server.ha.core.ConnectionState;

public interface HAClient {
    ConnectionState getConnectionState();
    void changeConnectionState(ConnectionState state);

    String getMasterAddress();
    String getMasterHaAddress();
    void setMasterAddress(String newAddress);
    void setMasterHaAddress(String newAddress);

    void wakeup();
    boolean connectMaster() throws Exception;
    void closeMaster();
}
