package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.HAClient;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import cn.coderule.minimq.store.server.ha.core.FlowMonitor;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/**
 * Report header buffer size. Schema: slaveMaxOffset. Format:
 *
 * <pre>
 * ┌───────────────────────────────────────────────┐
 * │                  slaveMaxOffset               │
 * │                    (8bytes)                   │
 * ├───────────────────────────────────────────────┤
 * │                                               │
 * │                  Report Header                │
 * </pre>
 * <p>
 */
@Slf4j
public class DefaultHAClient extends ServiceThread implements HAClient, Lifecycle {
    public static final int REPORT_HEADER_SIZE = 8;
    private static final int READ_MAX_BUFFER_SIZE = 4 * 1024 * 1024;

    private final StoreConfig storeConfig;

    private final AtomicReference<String> masterHaAddress = new AtomicReference<>();
    private final AtomicReference<String> masterAddress = new AtomicReference<>();
    private final ByteBuffer reportOffset = ByteBuffer.allocate(REPORT_HEADER_SIZE);
    private SocketChannel socketChannel;
    private final Selector selector;
    private final FlowMonitor flowMonitor;

    /**
     * last time that slave reads date from master.
     */
    private long lastReadTime = System.currentTimeMillis();
    /**
     * last time that slave reports offset to master.
     */
    private long lastWriteTime = System.currentTimeMillis();

    private long reportedOffset = 0;
    private int dispatchPosition = 0;
    private ByteBuffer readBuffer = ByteBuffer.allocate(READ_MAX_BUFFER_SIZE);
    private ByteBuffer backupBuffer = ByteBuffer.allocate(READ_MAX_BUFFER_SIZE);
    private volatile ConnectionState state = ConnectionState.READY;

    public DefaultHAClient(StoreConfig storeConfig) throws IOException {
        this.storeConfig = storeConfig;

        this.selector = NetworkUtil.openSelector();
        this.flowMonitor = new FlowMonitor(storeConfig);
    }

    @Override
    public String getServiceName() {
        return DefaultHAClient.class.getSimpleName();
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void initialize() {
        Lifecycle.super.initialize();
    }

    @Override
    public ConnectionState getConnectionState() {
        return state;
    }

    @Override
    public void changeConnectionState(ConnectionState state) {
        this.state = state;
    }

    @Override
    public void updateMasterAddress(String newAddress) {
        String addr = this.masterAddress.get();
        if (masterAddress.compareAndSet(addr, newAddress)) {
            log.info("update master address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public void updateMasterHaAddress(String newAddress) {
        String addr = this.masterHaAddress.get();
        if (masterHaAddress.compareAndSet(addr, newAddress)) {
            log.info("update master ha address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public boolean connectMaster() {
        return false;
    }

    @Override
    public void closeMaster() {

    }

    @Override
    public void run() {

    }
}
