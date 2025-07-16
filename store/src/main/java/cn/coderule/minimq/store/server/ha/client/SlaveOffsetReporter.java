package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

import static cn.coderule.minimq.store.server.ha.client.DefaultHAClient.REPORT_HEADER_SIZE;

@Slf4j
public class SlaveOffsetReporter implements Lifecycle {
    private static final int MAX_READ_BUFFER_SIZE = 4 * 1024 * 1024;

    private final StoreConfig storeConfig;
    private final DefaultHAClient haClient;
    private final CommitLogStore commitLogStore;

    private long lastWriteTime;
    private long reportedOffset = 0;
    private final ByteBuffer reportBuffer = ByteBuffer.allocate(REPORT_HEADER_SIZE);

    public SlaveOffsetReporter(DefaultHAClient haClient) {
        this.storeConfig = haClient.getStoreConfig();
        this.haClient = haClient;
        this.commitLogStore = haClient.getCommitLogStore();
        this.lastWriteTime = System.currentTimeMillis();
    }

    public boolean heartbeat() {
        if (!isTimeToHeartbeat()) {
            return true;
        }

        log.info("Slave report current offset {}", this.reportedOffset);
        return this.report(this.reportedOffset);
    }

    public boolean report() {
        long currentPhyOffset = commitLogStore.getMaxOffset();
        if (currentPhyOffset <= this.reportedOffset) {
            return true;
        }

        this.reportedOffset = currentPhyOffset;
        boolean result = this.report(this.reportedOffset);
        if (result) {
            return true;
        }

        haClient.closeMaster();
        cleanBuffer();
        log.error("HAClient, reportSlaveMaxOffset error, {}", this.reportedOffset);
        return false;
    }

    public boolean report(long offset) {
        this.reportBuffer.position(0);
        this.reportBuffer.limit(REPORT_HEADER_SIZE);
        this.reportBuffer.putLong(offset);
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

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void initialize() throws Exception {
        this.reportedOffset = commitLogStore.getMaxOffset();
    }

    private boolean isTimeToHeartbeat() {
        long interval = System.currentTimeMillis() - lastWriteTime;
        return interval > storeConfig.getHaHeartbeatInterval();
    }

    private void cleanBuffer() {
        this.reportBuffer.position(0);
        this.reportBuffer.limit(MAX_READ_BUFFER_SIZE);
    }
}
