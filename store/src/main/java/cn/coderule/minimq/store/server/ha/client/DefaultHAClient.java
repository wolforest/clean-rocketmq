package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.rpc.common.rpc.netty.service.helper.NettyHelper;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import cn.coderule.minimq.store.server.ha.core.monitor.FlowMonitor;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
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
    private static final int MAX_READ_BUFFER_SIZE = 4 * 1024 * 1024;

    private final StoreConfig storeConfig;

    private final AtomicReference<String> masterHaAddress = new AtomicReference<>();
    private final AtomicReference<String> masterAddress = new AtomicReference<>();
    private SocketChannel socketChannel;
    private final Selector selector;
    private final FlowMonitor flowMonitor;

    /**
     * last time that slave reads date from master.
     */
    private long lastReadTime;
    /**
     * last time that slave reports offset to master.
     */
    private long lastWriteTime;

    private long reportedOffset = 0;
    private int dispatchPosition = 0;

    private ByteBuffer readBuffer = ByteBuffer.allocate(MAX_READ_BUFFER_SIZE);
    private ByteBuffer backupBuffer = ByteBuffer.allocate(MAX_READ_BUFFER_SIZE);
    private final ByteBuffer reportBuffer = ByteBuffer.allocate(REPORT_HEADER_SIZE);

    private volatile ConnectionState state = ConnectionState.READY;

    public DefaultHAClient(StoreConfig storeConfig) throws IOException {
        this.storeConfig = storeConfig;

        this.selector = NetworkUtil.openSelector();
        this.flowMonitor = new FlowMonitor(storeConfig);

        long now = System.currentTimeMillis();
        this.lastReadTime = now;
        this.lastWriteTime = now;
    }

    @Override
    public String getServiceName() {
        return DefaultHAClient.class.getSimpleName();
    }

    @Override
    public void shutdown() {
        this.state = ConnectionState.SHUTDOWN;
        this.flowMonitor.shutdown();
        super.shutdown();

        closeMaster();
        try {
            this.selector.close();
        } catch (IOException e) {
            log.warn("Close the selector of AutoRecoverHAClient error, ", e);
        }
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
    public String getMasterAddress() {
        return masterAddress.get();
    }

    @Override
    public String getMasterHaAddress() {
        return masterHaAddress.get();
    }

    @Override
    public void setMasterAddress(String newAddress) {
        String addr = this.masterAddress.get();
        if (masterAddress.compareAndSet(addr, newAddress)) {
            log.info("update master address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public void setMasterHaAddress(String newAddress) {
        String addr = this.masterHaAddress.get();
        if (masterHaAddress.compareAndSet(addr, newAddress)) {
            log.info("update master ha address from {} to {}",
                addr, newAddress);
        }
    }

    @Override
    public boolean connectMaster() throws ClosedChannelException {
        if (null != socketChannel) {
            return true;
        }

        String addr = this.masterHaAddress.get();
        if (addr != null) {
            SocketAddress socketAddress = NetworkUtil.toSocketAddress(addr);
            this.socketChannel = NettyHelper.connect(socketAddress);
            if (this.socketChannel != null) {
                this.socketChannel.register(this.selector, SelectionKey.OP_READ);
                log.info("HAClient connect to master {}", addr);
                this.state = ConnectionState.TRANSFER;
            }
        }


        // this.currentReportedOffset = this.defaultMessageStore.getMaxPhyOffset();
        this.reportedOffset = 0;
        this.lastReadTime = System.currentTimeMillis();

        return this.socketChannel != null;
    }

    @Override
    public void closeMaster() {
        if (null == this.socketChannel) {
            return;
        }

        try {
            SelectionKey sk = this.socketChannel.keyFor(this.selector);
            if (sk != null) {
                sk.cancel();
            }

            this.socketChannel.close();
            this.socketChannel = null;

            log.info("HAClient close connection with master {}", this.masterHaAddress.get());
            this.state = ConnectionState.READY;
        } catch (IOException e) {
            log.warn("closeMaster exception. ", e);
        }

        this.lastReadTime = 0;
        this.dispatchPosition = 0;

        this.backupBuffer.position(0);
        this.backupBuffer.limit(MAX_READ_BUFFER_SIZE);

        this.reportBuffer.position(0);
        this.reportBuffer.limit(MAX_READ_BUFFER_SIZE);
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        this.flowMonitor.start();

        while (!this.isStopped()) {
            try {
                switch (this.state) {
                    case SHUTDOWN:
                        this.flowMonitor.shutdown(true);
                        return;
                    case READY:
                        if (!this.connectMaster()) {
                            log.warn("HAClient connect to master {} failed", this.masterHaAddress.get());
                            this.await(5_000);
                        }
                        continue;
                    case TRANSFER:
                        if (!transferFromMaster()) {
                            closeMasterAndWait();
                            continue;
                        }
                        break;
                    default:
                        this.await(2_000);
                        continue;
                }

                tryCloseMaster();
            } catch (Exception e) {
                log.warn("{} service has exception. ", this.getServiceName(), e);
                this.closeMasterAndWait();
            }
        }

        this.flowMonitor.shutdown(true);
        log.info("{} service end", this.getServiceName());
    }

    private void process() {

    }

    public void closeMasterAndWait() {
        this.closeMaster();
        this.await(5_000);
    }

    private void tryCloseMaster() {
        long interval = System.currentTimeMillis() - this.lastReadTime;
        if (interval <= storeConfig.getHaHouseKeepingInterval()) {
            return;
        }

        log.warn("AutoRecoverHAClient, housekeeping, found this connection[{}] expired, {}",
            this.masterHaAddress, interval);
        this.closeMaster();
        log.warn("AutoRecoverHAClient, master not response some time, so close connection");
    }

    private boolean transferFromMaster() throws IOException {
        boolean result;
        if (this.isTimeToHeartbeat()) {
            log.info("Slave report current offset {}", this.reportedOffset);
            result = this.reportSlaveOffset(this.reportedOffset);
            if (!result) {
                return false;
            }
        }

        this.selector.select(1000);

        result = this.processReadEvent();
        if (!result) {
            return false;
        }

        return reportSlaveOffset();
    }

    private boolean processReadEvent() {
        int emptyReadTimes = 0;
        while (this.readBuffer.hasRemaining()) {
            try {
                int readSize = this.socketChannel.read(this.readBuffer);
                if (readSize > 0) {
                    flowMonitor.addTransferredByte(readSize);
                    emptyReadTimes = 0;
                    boolean result = this.dispatchReadRequest();
                    if (!result) {
                        log.error("HAClient, dispatchReadRequest error");
                        return false;
                    }
                    lastReadTime = System.currentTimeMillis();
                } else if (readSize == 0) {
                    if (++emptyReadTimes >= 3) {
                        break;
                    }
                } else {
                    log.info("HAClient, processReadEvent read socket < 0");
                    return false;
                }
            } catch (IOException e) {
                log.info("HAClient, processReadEvent read socket exception", e);
                return false;
            }
        }

        return true;
    }

    private boolean dispatchReadRequest() {
        int readSocketPos = this.readBuffer.position();

        while (true) {
            int diff = this.readBuffer.position() - this.dispatchPosition;
            if (diff >= DefaultHAConnection.TRANSFER_HEADER_SIZE) {
                int bodySize = this.readBuffer.getInt(this.dispatchPosition + 8);

                long masterPhyOffset = this.readBuffer.getLong(this.dispatchPosition);
                // todo: get slave max commitLog offset
                long slavePhyOffset = 1;

                if (slavePhyOffset != 0) {
                    if (slavePhyOffset != masterPhyOffset) {
                        log.error("master pushed offset not equal the max phy offset in slave, SLAVE: " + slavePhyOffset + " MASTER: " + masterPhyOffset);
                        return false;
                    }
                }

                if (diff >= (DefaultHAConnection.TRANSFER_HEADER_SIZE + bodySize)) {
                    byte[] bodyData = readBuffer.array();
                    int dataStart = this.dispatchPosition + DefaultHAConnection.TRANSFER_HEADER_SIZE;

                    // todo
                    // this.defaultMessageStore.appendToCommitLog(masterPhyOffset, bodyData, dataStart, bodySize);
                    this.readBuffer.position(readSocketPos);
                    this.dispatchPosition += DefaultHAConnection.TRANSFER_HEADER_SIZE + bodySize;

                    if (!reportSlaveOffset()) {
                        return false;
                    }

                    continue;
                }
            }

            if (!this.readBuffer.hasRemaining()) {
                this.reallocateBuffer();
            }

            break;
        }

        return true;
    }

    private boolean reportSlaveOffset() {
        // todo: this.defaultMessageStore.getMaxPhyOffset()
        long currentPhyOffset = 10;
        if (currentPhyOffset <= this.reportedOffset) {
            return true;
        }

        this.reportedOffset = currentPhyOffset;
        boolean result = this.reportSlaveOffset(this.reportedOffset);
        if (result) {
            return true;
        }

        this.closeMaster();
        log.error("HAClient, reportSlaveMaxOffset error, {}", this.reportedOffset);
        return false;
    }

    private boolean reportSlaveOffset(final long maxOffset) {
        this.reportBuffer.position(0);
        this.reportBuffer.limit(REPORT_HEADER_SIZE);
        this.reportBuffer.putLong(maxOffset);
        this.reportBuffer.position(0);
        this.reportBuffer.limit(REPORT_HEADER_SIZE);

        for (int i = 0; i < 3 && this.reportBuffer.hasRemaining(); i++) {
            try {
                this.socketChannel.write(this.reportBuffer);
            } catch (IOException e) {
                log.error("{} reportSlaveMaxOffset this.socketChannel.write exception",
                    this.getServiceName(), e);
                return false;
            }
        }
        lastWriteTime = System.currentTimeMillis();
        return !this.reportBuffer.hasRemaining();
    }

    private void reallocateBuffer() {
        int remain = MAX_READ_BUFFER_SIZE - this.dispatchPosition;
        if (remain > 0) {
            this.readBuffer.position(this.dispatchPosition);

            this.backupBuffer.position(0);
            this.backupBuffer.limit(MAX_READ_BUFFER_SIZE);
            this.backupBuffer.put(this.readBuffer);
        }

        this.swapBuffer();

        this.readBuffer.position(remain);
        this.readBuffer.limit(MAX_READ_BUFFER_SIZE);
        this.dispatchPosition = 0;
    }

    private void swapBuffer() {
        ByteBuffer tmp = this.readBuffer;
        this.readBuffer = this.backupBuffer;
        this.backupBuffer = tmp;
    }

    private boolean isTimeToHeartbeat() {
        long interval = System.currentTimeMillis() - lastWriteTime;
        return interval > storeConfig.getHaHeartbeatInterval();
    }

}
