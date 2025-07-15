package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import java.nio.channels.SocketChannel;

public interface HAConnection {
    SocketChannel getSocketChannel();
    String getClientAddress();

    ConnectionState getConnectionState();
    void setConnectionState(ConnectionState state);

    ConnectionContext getContext();
    long getSlaveOffset();

    void start() throws Exception;
    void close();
}
