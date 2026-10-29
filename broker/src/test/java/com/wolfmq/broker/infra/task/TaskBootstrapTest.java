package com.wolfmq.broker.infra.task;

import cn.coderule.common.convention.container.ApplicationContext;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskBootstrapTest {
    private final ApplicationContext context = new ApplicationContext();


    @Test
    void shutdown_ShouldNotThrow() {
        TaskBootstrap bootstrap = new TaskBootstrap(context);
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}
