package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.monitor.FlowMonitor;
import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogTransfer extends ServiceThread implements Lifecycle {
    private final StoreConfig storeConfig;
    private final HAConnection connection;
    private final CommitLogStore commitLogStore;
    private final FlowMonitor flowMonitor;

    private final Selector selector;
    private final SocketChannel socketChannel;
    private final ByteBuffer headerBuffer;
    private SelectedMappedBuffer selectedBuffer;

    private boolean transferDone = true;
    private long nextTransferOffset;
    private long lastWriteTime;
    private long lastReportTime;

    public CommitLogTransfer(HAConnection connection) throws IOException {
        this.connection = connection;
        ConnectionContext context = connection.getContext();
        this.storeConfig = context.getStoreConfig();
        this.commitLogStore = context.getCommitLogStore();
        this.flowMonitor = context.getFlowMonitor();

        this.socketChannel = connection.getSocketChannel();
        this.selector = connection.openSelector();
        connection.registerSelector(selector, SelectionKey.OP_WRITE);
        this.setDaemon(true);

        long now = System.currentTimeMillis();
        this.nextTransferOffset = -1;
        this.headerBuffer = ByteBuffer.allocate(DefaultHAConnection.TRANSFER_HEADER_SIZE);
        this.lastWriteTime = now;
        this.lastReportTime = now;
    }

    @Override
    public String getServiceName() {
        return CommitLogTransfer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                this.selector.select(1_000);
                if (-1 == connection.getSlaveOffset()) {
                    ThreadUtil.sleep(10);
                    continue;
                }

                initOffset();
                if (!transferUnfinishedData()) {
                    continue;
                }

                transfer();
            } catch (Exception e) {
                log.error("{} service has exception. ", this.getServiceName(), e);
                break;
            }
        }

        releaseResource();
        log.info("{} service end", this.getServiceName());
    }

    private void transfer() {

    }

    private void initOffset() {
        if (-1 == this.nextTransferOffset) {
            return;
        }

        long slaveOffset = connection.getSlaveOffset();
        if (0 != slaveOffset) {
            this.nextTransferOffset = slaveOffset;
        } else {
            this.nextTransferOffset = getMasterOffset();
        }

        log.info("{} init offset: nextTransferOffset={}, slaveOffset={}",
            this.getServiceName(), this.nextTransferOffset, slaveOffset);
    }

    private long getMasterOffset() {
        CommitConfig commitConfig = storeConfig.getCommitConfig();
        long masterOffset = commitLogStore.getMaxOffset();
        long fileSize = commitConfig.getFileSize();

        masterOffset = masterOffset - (masterOffset % fileSize);
        if (masterOffset < 0) {
            masterOffset = 0;
        }

        return masterOffset;
    }

    private boolean transferUnfinishedData() {
        if (!this.transferDone) {
            this.transferDone = transferData();
            return this.transferDone;
        }

        long interval = System.currentTimeMillis() - this.lastWriteTime;
        long heartbeatInterval = storeConfig.getHaHeartbeatInterval();
        if (interval <= heartbeatInterval) {
            return true;
        }

        initHeaderBuffer();

        this.transferDone = transferData();
        return transferDone;
    }

    private void initHeaderBuffer() {
        this.headerBuffer.position(0);
        this.headerBuffer.limit(DefaultHAConnection.TRANSFER_HEADER_SIZE);
        this.headerBuffer.putLong(this.nextTransferOffset);
        this.headerBuffer.putInt(0);
        this.headerBuffer.flip();
    }

    private boolean transferData() throws Exception {
        writeHeader();

        if (null == this.selectedBuffer) {
            return !this.headerBuffer.hasRemaining();
        }

        writeBody();

        boolean result = !headerBuffer.hasRemaining()
            && !selectedBuffer.getByteBuffer().hasRemaining();

        if (!selectedBuffer.getByteBuffer().hasRemaining()) {
            this.selectedBuffer.release();
            this.selectedBuffer = null;
        }

        return result;
    }

    private void writeHeader() throws Exception {
        int writeSizeZeroTimes = 0;
        // Write Header
        while (this.headerBuffer.hasRemaining()) {
            int writeSize = this.socketChannel.write(this.headerBuffer);
            if (writeSize > 0) {
                flowMonitor.addTransferredByte(writeSize);
                writeSizeZeroTimes = 0;
                this.lastWriteTime = System.currentTimeMillis();
            } else if (writeSize == 0) {
                if (++writeSizeZeroTimes >= 3) {
                    break;
                }
            } else {
                throw new Exception("ha master write header error < 0");
            }
        }
    }

    private void writeBody() throws Exception {
        int writeSizeZeroTimes = 0;
        if (this.headerBuffer.hasRemaining()) {
            return;
        }

        while (this.selectedBuffer.getByteBuffer().hasRemaining()) {
            int writeSize = this.socketChannel.write(this.selectedBuffer.getByteBuffer());
            if (writeSize > 0) {
                writeSizeZeroTimes = 0;
                this.lastWriteTime = System.currentTimeMillis();
            } else if (writeSize == 0) {
                if (++writeSizeZeroTimes >= 3) {
                    break;
                }
            } else {
                throw new Exception("ha master write body error < 0");
            }
        }
    }

    private void releaseResource() {
        ConnectionContext context = connection.getContext();
        context.getWakeupCoordinator().removeCurrentThread();
        if (null != this.selectedBuffer) {
            selectedBuffer.release();
        }

        connection.setConnectionState(ConnectionState.SHUTDOWN);
        this.stop();
        context.getCommitLogTransfer().stop();
        context.getConnectionPool().removeConnection(connection);

        cancelSelectionKey();
        closeChannel();
    }

    private void closeChannel() {
        try {
            this.selector.close();
            this.socketChannel.close();
        } catch (Exception e) {
            log.error("{} release resources error", this.getServiceName(), e);
        }
    }

    private void cancelSelectionKey() {
        SelectionKey key = connection.keyFor(selector);
        if (key == null) {
            return;
        }
        key.cancel();
    }

}
