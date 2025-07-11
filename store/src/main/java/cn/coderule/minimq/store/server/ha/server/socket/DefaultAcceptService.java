package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.core.HAConnection;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import cn.coderule.minimq.store.server.ha.server.ConnectionContext;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DefaultAcceptService extends AbstractAcceptService {

    public DefaultAcceptService(StoreConfig storeConfig, ConnectionPool connectionPool, LifecycleManager resourcePool) {
        super(storeConfig, connectionPool, resourcePool);
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
                .resourcePool(resourcePool)
                .connectionPool(connectionPool)
                .socketChannel(socketChannel)
                .build();
    }
}
