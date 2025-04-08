package cn.coderule.minimq.store.server.ha;

import cn.coderule.common.convention.service.Lifecycle;

/**
 * cluster service:
 * - communicate with registry
 *   - register master
 *   - register slave if no master registered
 *   - registry heartbeat
 * - initialize M/S
 *   - send ha info
 *   - handshake
 *   - syn metadata
 */
public class ClusterService implements Lifecycle {
    @Override
    public void initialize() {
    }

    @Override
    public void start() {

    }

    @Override
    public void shutdown() {

    }


}
