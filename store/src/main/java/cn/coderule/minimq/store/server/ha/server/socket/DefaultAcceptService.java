package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import cn.coderule.minimq.store.server.ha.core.WakeupCoordinator;
import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * socket accept service
 * - bind port (AbstractAcceptService)
 * - accept new connection (AbstractAcceptService)
 * - create connection
 *   - create connection context
 *   - create connection with context
 */
public class DefaultAcceptService extends AbstractAcceptService {

    public DefaultAcceptService(
        StoreConfig storeConfig,
        ConnectionPool connectionPool,
        LifecycleManager resourcePool,
        WakeupCoordinator wakeupCoordinator
    ) {
        super(storeConfig, connectionPool, resourcePool, wakeupCoordinator);
    }

    @Override
    protected HAConnection createConnection(SocketChannel sc) throws IOException {
        ConnectionContext context = buildContext(sc);
        return new DefaultHAConnection(context);
    }

    @Override
    public String getServiceName() {
        return DefaultAcceptService.class.getSimpleName();
    }

    private ConnectionContext buildContext(SocketChannel socketChannel) {
        return ConnectionContext.builder()
            .storeConfig(storeConfig)
            .wakeupCoordinator(wakeupCoordinator)
            .resourcePool(resourcePool)
            .connectionPool(connectionPool)
            .socketChannel(socketChannel)
            .build();
    }
}
