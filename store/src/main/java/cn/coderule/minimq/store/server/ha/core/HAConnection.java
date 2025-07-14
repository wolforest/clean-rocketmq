package cn.coderule.minimq.store.server.ha.core;

import java.nio.channels.SocketChannel;

public interface HAConnection {
    SocketChannel getSocketChannel();
    String getClientAddress();

    ConnectionState getConnectionState();
    void setConnectionState(ConnectionState state);

    long getSlaveOffset();

    void start() throws Exception;
    void close();
}
