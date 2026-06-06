package cn.coderule.wolfmq.broker.domain.timer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimerBootstrapTest {

    private final TimerBootstrap bootstrap = new TimerBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}