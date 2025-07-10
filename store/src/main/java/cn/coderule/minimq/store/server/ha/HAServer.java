package cn.coderule.minimq.store.server.ha;

import java.util.List;

public interface HAServer {
    List<HAConnection> getConnectionList();
}
