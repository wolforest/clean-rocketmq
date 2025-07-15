package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogTransfer extends ServiceThread implements Lifecycle {
    private CommitLogStore commitLogStore;
    private final StoreConfig storeConfig;
    private final HAConnection connection;

    private final Selector selector;
    private final SocketChannel socketChannel;
    private final ByteBuffer headerBuffer;
    private SelectedMappedBuffer selectedBuffer;

    private boolean transferDone = true;
    private long nextTransferOffset;
    private long lastWriteTime;
    private long lastReportTime;

    public CommitLogTransfer(HAConnection connection) throws IOException {
        this.storeConfig = connection.getContext().getStoreConfig();
        this.connection = connection;
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
        return true;
    }

    private void transfer() {

    }

    private void releaseResource() {

    }
}
