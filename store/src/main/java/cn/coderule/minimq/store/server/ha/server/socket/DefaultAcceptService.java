package cn.coderule.minimq.store.server.ha.server.socket;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.HAConnection;
import cn.coderule.minimq.store.server.ha.core.DefaultHAConnection;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class DefaultAcceptService extends AbstractAcceptService {

    public DefaultAcceptService(StoreConfig storeConfig, ConnectionPool connectionPool) {
        super(storeConfig, connectionPool);
    }

    @Override
    protected HAConnection createConnection(SocketChannel sc) throws IOException {
        return new DefaultHAConnection(storeConfig, sc);
    }

    @Override
    public String getServiceName() {
        return DefaultAcceptService.class.getSimpleName();
    }
}
