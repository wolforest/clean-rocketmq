package cn.coderule.minimq.store.server.ha.server;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.common.lang.concurrent.thread.WakeupCoordinator;
import cn.coderule.common.util.net.NetworkUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.HAContext;
import cn.coderule.minimq.store.server.ha.server.socket.DefaultAcceptService;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultHAServer implements HAServer {
    private final StoreConfig storeConfig;

    protected final SocketAddress socketAddress;
    protected ServerSocketChannel serverSocketChannel;
    protected Selector selector;

    @Getter
    private HAContext haContext;

    private final DefaultAcceptService acceptService;

    public DefaultHAServer(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;

        int port = storeConfig.getHaPort();
        this.socketAddress = new InetSocketAddress(port);

        this.buildHAContext();
        this.acceptService = new DefaultAcceptService(haContext);
    }

    @Override
    public List<HAConnection> getConnectionList() {
        return haContext.getConnectionPool().getConnectionList();
    }

    @Override
    public void start() throws Exception {
        try {
            this.bind();
            acceptService.start();
        } catch (Throwable t) {
            log.error("start DefaultHAServer error", t);
        }
    }

    @Override
    public void shutdown() throws Exception {
        try {
            acceptService.shutdown();

            if (null != this.serverSocketChannel) {
                this.serverSocketChannel.close();
            }

            if (null != this.selector) {
                this.selector.close();
            }

        } catch (Throwable t) {
            log.error("shutdown DefaultHAServer error", t);
        }
    }

    private void bind() throws Exception {
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

        haContext.setSelector(selector);
        haContext.setServerSocketChannel(serverSocketChannel);
    }

    private void buildHAContext() {
        haContext = HAContext.builder()
                .storeConfig(storeConfig)
                .resourcePool(new LifecycleManager())
                .connectionPool(new ConnectionPool())
                .wakeupCoordinator(new WakeupCoordinator())
                .socketAddress(socketAddress)
                .build();
    }


}
