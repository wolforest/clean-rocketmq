package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.minimq.store.server.ha.HAConnection;
import java.nio.channels.SocketChannel;

public class DefaultHAConnection implements HAConnection {
    /**
     * Transfer Header buffer size. Schema: physic offset and body size. Format:
     *
     * <pre>
     * ┌───────────────────────────────────────────────┬───────────────────────┐
     * │                  physicOffset                 │         bodySize      │
     * │                    (8bytes)                   │         (4bytes)      │
     * ├───────────────────────────────────────────────┴───────────────────────┤
     * │                                                                       │
     * │                           Transfer Header                             │
     * </pre>
     * <p>
     */
    public static final int TRANSFER_HEADER_SIZE = 8 + 4;
    @Override
    public SocketChannel getSocketChannel() {
        return null;
    }

    @Override
    public ConnectionState getConnectionState() {
        return null;
    }
}
