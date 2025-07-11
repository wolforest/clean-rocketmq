package cn.coderule.minimq.store.server.ha.core;

import cn.coderule.common.convention.service.LifecycleManager;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.store.server.ha.server.ConnectionPool;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HAContext implements Serializable {
    private StoreConfig storeConfig;

    private LifecycleManager resourcePool;
    private ConnectionPool connectionPool;
    private WakeupCoordinator wakeupCoordinator;
}
