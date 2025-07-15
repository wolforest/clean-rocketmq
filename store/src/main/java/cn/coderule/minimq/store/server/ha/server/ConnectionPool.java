package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.minimq.store.server.ha.core.HAConnection;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;

@Data
public class ConnectionPool implements Serializable {
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    private final List<HAConnection> connectionList = new LinkedList<>();

    public void addConnection(HAConnection connection) {
        connectionList.add(connection);
        connectionCount.incrementAndGet();
    }

    public void removeConnection(HAConnection connection) {
        connectionList.remove(connection);
        connectionCount.decrementAndGet();
    }

    public int getConnectionCount() {
        return connectionCount.get();
    }
}
