package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public interface HAConnection {
    ConnectionContext getContext();

    SocketChannel getSocketChannel();
    String getClientAddress();

    Selector openSelector() throws IOException;
    SelectionKey registerSelector(Selector selector, int ops) throws ClosedChannelException;
    SelectionKey keyFor(Selector selector);

    ConnectionState getConnectionState();
    void setConnectionState(ConnectionState state);

    boolean isSlaveHealthy(long masterOffset);
    long getSlaveOffset();

    void start() throws Exception;
    void close();
}
