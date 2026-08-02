package cn.coderule.wolfmq.broker.infra.task;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskBootstrapTest {

    @Test
    void shutdown_ShouldNotThrow() {
        TaskBootstrap bootstrap = new TaskBootstrap();
        assertDoesNotThrow(() -> bootstrap.shutdown());
    }
}
