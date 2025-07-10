package cn.coderule.minimq.store.server.ha;

import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import java.nio.channels.SocketChannel;

public interface HAConnection {
    SocketChannel getSocketChannel();
    String getClientAddress();

    ConnectionState getConnectionState();
    void setConnectionState(ConnectionState state);

    void start();
    void close();
}
