package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.ConnectionState;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SlaveOffsetReceiver extends ServiceThread implements Serializable, Lifecycle {
    private static final int READ_MAX_BUFFER_SIZE = 1024 * 1024;

    private StoreConfig storeConfig;
    private ConnectionPool connectionPool;
    private HAConnection connection;

    @Getter @Setter
    private volatile long requestOffset = -1;
    @Getter @Setter
    private volatile long ackOffset = -1;

    private Selector selector;
    private SocketChannel socketChannel;
    private ByteBuffer readBuffer;

    private int processPosition;
    private volatile long lastReadTime;

    public SlaveOffsetReceiver(HAConnection connection) {
        this.connection = connection;

        this.readBuffer = ByteBuffer.allocate(READ_MAX_BUFFER_SIZE);
        this.lastReadTime = System.currentTimeMillis();
        this.processPosition = 0;

        this.setDaemon(true);
    }

    @Override
    public String getServiceName() {
        return SlaveOffsetReceiver.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                this.selector.select(1_000);
                boolean ok = this.receive();
                if (!ok) {
                    log.error("{} receive slave offset failed", this.getServiceName());
                    break;
                }

                long interval = System.currentTimeMillis() - this.lastReadTime;
                if (interval > storeConfig.getHaHouseKeepingInterval()) {
                    log.warn("{} housekeeping, found this connection[{}] expired, {}",
                        this.getServiceName(), this.connection.getClientAddress(), interval);
                    break;
                }
            } catch (Exception e) {
                log.error("{} occurs exception.", this.getServiceName(), e);
                break;
            }
        }

        this.releaseResources();
        log.info("{} service end", this.getServiceName());
    }

    private boolean receive() {
        return true;
    }

    private void releaseResources() {
        connection.setConnectionState(ConnectionState.SHUTDOWN);
        this.stop();

        // stop commitLog transfer
        connectionPool.removeConnection(connection);

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
