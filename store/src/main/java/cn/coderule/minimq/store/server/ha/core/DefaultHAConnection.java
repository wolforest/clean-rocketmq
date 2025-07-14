package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.rpc.common.rpc.config.RpcSystemConfig;
import cn.coderule.minimq.store.server.ha.core.monitor.FlowMonitor;
import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import cn.coderule.minimq.store.server.ha.server.processor.SlaveMonitor;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultHAConnection implements HAConnection, Lifecycle {
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

    private final ConnectionContext context;

    private final StoreConfig storeConfig;
    private final SocketChannel socketChannel;
    private final String clientAddress;

    private volatile long slaveRequestOffset = -1;
    private volatile long slaveAckOffset = -1;

    private volatile ConnectionState state = ConnectionState.TRANSFER;
    private final FlowMonitor flowMonitor;
    private final SlaveMonitor slaveMonitor;

    public DefaultHAConnection(ConnectionContext context) throws IOException {
        this.context = context;
        this.storeConfig = context.getStoreConfig();
        this.socketChannel = context.getSocketChannel();
        this.clientAddress = socketChannel.socket().getRemoteSocketAddress().toString();

        this.flowMonitor = new FlowMonitor(storeConfig);
        this.slaveMonitor = new SlaveMonitor();

        this.configureSocketChannel();
    }

    @Override
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    @Override
    public String getClientAddress() {
        return clientAddress;
    }

    @Override
    public ConnectionState getConnectionState() {
        return state;
    }

    @Override
    public void setConnectionState(ConnectionState state) {
        this.state = state;
    }

    @Override
    public long getSlaveOffset() {
        return slaveMonitor.getAckOffset();
    }

    @Override
    public void close() {
        if (this.socketChannel == null) {
            return;
        }

        try {
            this.socketChannel.close();
        } catch (IOException e) {
            log.error("", e);
        }
    }

    @Override
    public void start() throws Exception {
        this.state = ConnectionState.TRANSFER;

        this.flowMonitor.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.state = ConnectionState.SHUTDOWN;
        this.flowMonitor.shutdown();
        this.close();
    }

    private void configureSocketChannel() throws IOException {
        this.socketChannel.configureBlocking(false);
        this.socketChannel.socket().setSoLinger(false, -1);
        this.socketChannel.socket().setTcpNoDelay(true);
        if (RpcSystemConfig.socketSndbufSize > 0) {
            this.socketChannel.socket().setReceiveBufferSize(RpcSystemConfig.socketSndbufSize);
        }
        if (RpcSystemConfig.socketRcvbufSize > 0) {
            this.socketChannel.socket().setSendBufferSize(RpcSystemConfig.socketRcvbufSize);
        }
    }


}
