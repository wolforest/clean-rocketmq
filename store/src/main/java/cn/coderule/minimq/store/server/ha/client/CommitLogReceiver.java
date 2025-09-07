package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.cluster.store.api.CommitLogStore;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogReceiver implements Lifecycle {
    private static final int MAX_READ_BUFFER_SIZE = 4 * 1024 * 1024;

    private final DefaultHAClient haClient;
    private final SlaveOffsetReporter slaveOffsetReporter;
    private final CommitLogStore commitLogStore;

    /**
     * last time that slave reads date from master.
     */
    @Getter
    private long lastReadTime;
    private int dispatchPosition = 0;

    private ByteBuffer readBuffer = ByteBuffer.allocate(MAX_READ_BUFFER_SIZE);
    private ByteBuffer backupBuffer = ByteBuffer.allocate(MAX_READ_BUFFER_SIZE);

    public CommitLogReceiver(DefaultHAClient haClient) {
        this.haClient = haClient;
        this.commitLogStore = haClient.getCommitLogStore();
        this.slaveOffsetReporter = new SlaveOffsetReporter(haClient);

        this.lastReadTime = System.currentTimeMillis();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void shutdown() throws Exception {
    }

    @Override
    public void initialize() throws Exception {
        slaveOffsetReporter.initialize();
        this.lastReadTime = System.currentTimeMillis();
    }

    public boolean receive() throws IOException {
        if (!slaveOffsetReporter.heartbeat()) {
            return false;
        }

        haClient.getSelector().select(1000);

        if (!this.processReadEvent()) {
            return false;
        }

        return slaveOffsetReporter.report();
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
        int headerSize = DefaultHAConnection.TRANSFER_HEADER_SIZE;

        while (true) {
            int diff = this.readBuffer.position() - this.dispatchPosition;

            if (diff >= headerSize) {
                int bodySize = this.readBuffer.getInt(this.dispatchPosition + 8);
                long masterOffset = this.readBuffer.getLong(this.dispatchPosition);

                if (!validateSlaveOffset(masterOffset)) {
                    return false;
                }

                if (diff >= headerSize + bodySize) {
                    if (!appendCommitLog(masterOffset, readSocketPos, bodySize)) {
                        return false;
                    }

                    continue;
                }
            }

            cleanDirtyBuffer();
            break;
        }

        return true;
    }

    private void cleanDirtyBuffer() {
        if (this.readBuffer.hasRemaining()) {
            return;
        }

        this.reallocateBuffer();
    }

    private boolean validateSlaveOffset(long masterOffset) {
        long slaveOffset = commitLogStore.getMaxOffset();
        if (slaveOffset == 0) {
            return true;
        }

        if (slaveOffset != masterOffset) {
            log.error("master pushed offset not equal the max phy offset in slave, SLAVE: {} MASTER: {}", slaveOffset, masterOffset);
            return false;
        }

        return true;
    }

    private boolean appendCommitLog(long masterOffset, int readSocketPos, int bodySize) {
        byte[] bodyData = readBuffer.array();
        int dataStart = this.dispatchPosition + DefaultHAConnection.TRANSFER_HEADER_SIZE;

        commitLogStore.insert(masterOffset, bodyData, dataStart, bodySize);
        this.readBuffer.position(readSocketPos);
        this.dispatchPosition += DefaultHAConnection.TRANSFER_HEADER_SIZE + bodySize;

        return slaveOffsetReporter.report();
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

}
