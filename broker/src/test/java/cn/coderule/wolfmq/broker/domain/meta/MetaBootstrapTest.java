package cn.coderule.wolfmq.broker.domain.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MetaBootstrapTest {

    private final MetaBootstrap bootstrap = new MetaBootstrap();

    @Test
    void testImplementsLifecycle() {
        assertInstanceOf(cn.coderule.common.convention.service.Lifecycle.class, bootstrap);
    }
}