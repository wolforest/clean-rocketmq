package com.wolfmq.store.server.ha.server;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.store.server.ha.core.HAConnection;
import com.wolfmq.store.server.ha.core.HAContext;
import java.util.List;

public interface HAServer extends Lifecycle {
    List<HAConnection> getConnectionList();
    HAContext getHaContext();
}
