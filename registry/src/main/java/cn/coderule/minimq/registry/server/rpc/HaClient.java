package cn.coderule.minimq.registry.server.rpc;

import cn.coderule.common.convention.service.Lifecycle;
import java.util.Map;

public class HaClient implements Lifecycle {
    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }

    public void notifyRoleChanged(Map<Long, String> addrMap, String haAddr) {
        notifyRoleChanged(addrMap, haAddr, null);
    }

    public void notifyRoleChanged(Map<Long, String> addrMap, String haAddr, String offlineAddr) {

    }
}
