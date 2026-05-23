package cn.coderule.wolfmq.broker.infra.remote;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskStrategy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RemoteStoreBootstrapTest {

    @Test
    void testRemoteStoreBootstrap_constructor() {
        RemoteStoreBootstrap bootstrap = new RemoteStoreBootstrap();
        assertNotNull(bootstrap);
    }
}