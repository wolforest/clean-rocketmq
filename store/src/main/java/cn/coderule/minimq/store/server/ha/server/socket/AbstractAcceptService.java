package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.WakeupCoordinator;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractAcceptService extends ServiceThread {
    protected final SocketAddress socketAddress;
    protected ServerSocketChannel serverSocketChannel;
    protected Selector selector;

    protected final StoreConfig storeConfig;
    protected final ConnectionPool connectionPool;
    protected final LifecycleManager resourcePool;
    protected final WakeupCoordinator wakeupCoordinator;

    public AbstractAcceptService(
        StoreConfig storeConfig,
        ConnectionPool connectionPool,
        LifecycleManager resourcePool,
        WakeupCoordinator wakeupCoordinator
    ) {
        this.storeConfig = storeConfig;
        this.connectionPool = connectionPool;
        this.resourcePool = resourcePool;
        this.wakeupCoordinator = wakeupCoordinator;

        this.socketAddress = new InetSocketAddress(storeConfig.getHaPort());
    }

    /**
     * Create ha connection
     */
    protected abstract HAConnection createConnection(final SocketChannel sc) throws IOException;

    /**
     * Starts listening to slave connections.
     *
     * @throws Exception If fails.
     */
    public void beginAccept() throws Exception {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.selector = NetworkUtil.openSelector();
        this.serverSocketChannel.socket().setReuseAddress(true);
        this.serverSocketChannel.socket().bind(this.socketAddress);

        if (0 == storeConfig.getHaPort()) {
            int port = serverSocketChannel.socket().getLocalPort();
            storeConfig.setHaPort(port);
            log.info("OS picked up {} to listen for HA", storeConfig.getHaPort());
        }

        this.serverSocketChannel.configureBlocking(false);
        this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(final boolean interrupt) {
        super.shutdown(interrupt);
        try {
            if (null != this.serverSocketChannel) {
                this.serverSocketChannel.close();
            }

            if (null != this.selector) {
                this.selector.close();
            }
        } catch (IOException e) {
            log.error("AcceptSocketService shutdown exception", e);
        }
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
