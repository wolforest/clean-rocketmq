package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static cn.coderule.minimq.store.server.ha.client.DefaultHAClient.REPORT_HEADER_SIZE;

@Slf4j
public class CommitLogReceiver implements Lifecycle {
    private static final int MAX_READ_BUFFER_SIZE = 4 * 1024 * 1024;

    private final StoreConfig storeConfig;
    private final DefaultHAClient haClient;
    private CommitLogStore commitLogStore;

    /**
     * last time that slave reads date from master.
     */
    @Getter
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

    public CommitLogReceiver(StoreConfig storeConfig, DefaultHAClient haClient) {
        this.storeConfig = storeConfig;
        this.haClient = haClient;

        long now = System.currentTimeMillis();
        this.lastReadTime = now;
        this.lastWriteTime = now;
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void initialize() throws Exception {
        // this.currentReportedOffset = this.defaultMessageStore.getMaxPhyOffset();
        this.reportedOffset = commitLogStore.getMaxOffset();
        this.lastReadTime = System.currentTimeMillis();
    }

    public boolean receive() throws IOException {
        boolean result;
        if (this.isTimeToHeartbeat()) {
            log.info("Slave report current offset {}", this.reportedOffset);
            result = this.reportSlaveOffset(this.reportedOffset);
            if (!result) {
                return false;
            }
        }

        haClient.getSelector().select(1000);

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
                int readSize = haClient.getSocketChannel().read(this.readBuffer);
                if (readSize > 0) {
                    haClient.getFlowMonitor().addTransferredByte(readSize);
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
                long slavePhyOffset = commitLogStore.getMaxOffset();

                if (slavePhyOffset != 0) {
                    if (slavePhyOffset != masterPhyOffset) {
                        log.error("master pushed offset not equal the max phy offset in slave, SLAVE: {} MASTER: {}", slavePhyOffset, masterPhyOffset);
                        return false;
                    }
                }

                if (diff >= (DefaultHAConnection.TRANSFER_HEADER_SIZE + bodySize)) {
                    byte[] bodyData = readBuffer.array();
                    int dataStart = this.dispatchPosition + DefaultHAConnection.TRANSFER_HEADER_SIZE;

                    // todo
                    // this.defaultMessageStore.appendToCommitLog(masterPhyOffset, bodyData, dataStart, bodySize);
                    commitLogStore.insert(masterPhyOffset, bodyData, dataStart, bodySize);
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

        haClient.closeMaster();
        cleanBuffer();
        log.error("HAClient, reportSlaveMaxOffset error, {}", this.reportedOffset);
        return false;
    }

    private void cleanBuffer() {
        this.lastReadTime = 0;
        this.dispatchPosition = 0;

        this.backupBuffer.position(0);
        this.backupBuffer.limit(MAX_READ_BUFFER_SIZE);

        this.reportBuffer.position(0);
        this.reportBuffer.limit(MAX_READ_BUFFER_SIZE);
    }

    private boolean reportSlaveOffset(final long maxOffset) {
        this.reportBuffer.position(0);
        this.reportBuffer.limit(REPORT_HEADER_SIZE);
        this.reportBuffer.putLong(maxOffset);
        this.reportBuffer.position(0);
        this.reportBuffer.limit(REPORT_HEADER_SIZE);

        for (int i = 0; i < 3 && this.reportBuffer.hasRemaining(); i++) {
            try {
                haClient.getSocketChannel().write(this.reportBuffer);
            } catch (IOException e) {
                log.error("reportSlaveMaxOffset this.socketChannel.write exception",
                    e);
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
