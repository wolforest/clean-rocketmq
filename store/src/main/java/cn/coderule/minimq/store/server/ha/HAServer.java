package cn.coderule.minimq.store.server.ha;

import cn.coderule.minimq.store.server.ha.core.HAConnection;
import java.util.List;

public interface HAServer {
    List<HAConnection> getConnectionList();
}
