package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.HAContext;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAcceptService extends ServiceThread {
    protected Selector selector;

    protected HAContext haContext;
    protected final StoreConfig storeConfig;
    protected final ConnectionPool connectionPool;
    protected final LifecycleManager resourcePool;
    protected final WakeupCoordinator wakeupCoordinator;

    public AbstractAcceptService(HAContext haContext) {
        this.haContext = haContext;
        this.storeConfig = haContext.getStoreConfig();
        this.connectionPool = haContext.getConnectionPool();
        this.resourcePool = haContext.getResourcePool();
        this.wakeupCoordinator = haContext.getWakeupCoordinator();
        this.selector = haContext.getSelector();
    }

    /**
     * Create ha connection
     */
    protected abstract HAConnection createConnection(final SocketChannel sc) throws IOException;


    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(final boolean interrupt) {
        super.shutdown(interrupt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            try {
                this.selector.select(1000);
                Set<SelectionKey> selected = this.selector.selectedKeys();
                if (null == selected) {
                    continue;
                }

                accept(selected);
                selected.clear();
            } catch (Exception e) {
                log.error("{} service has exception.",
                    this.getServiceName(), e);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    protected void accept(Set<SelectionKey> selected) throws IOException {
        for (SelectionKey k : selected) {
            if (!k.isAcceptable()) {
                log.warn("Unexpected ops in select {}", k.readyOps());
                continue;
            }

            ServerSocketChannel ss = (ServerSocketChannel) k.channel();
            SocketChannel sc = ss.accept();
            if (null == sc) {
                continue;
            }

            createAndStartConnection(sc);
        }
    }

    protected void createAndStartConnection(SocketChannel sc) throws IOException {
        log.info("HAService receive new connection, {}",
            sc.socket().getRemoteSocketAddress());

        try {
            HAConnection conn = createConnection(sc);
            conn.start();
            connectionPool.addConnection(conn);
        } catch (Exception e) {
            log.error("new HAConnection exception", e);
            sc.close();
        }
    }


}
