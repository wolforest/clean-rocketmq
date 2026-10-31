package com.wolfmq.broker.server.bootstrap;

import cn.coderule.common.convention.container.ApplicationContext;
import com.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    private final ApplicationContext context = new ApplicationContext();

    @Test
    void testLoad() {
        ConfigLoader.load(context);

        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class);
        assertNotNull(brokerConfig);
        assertNotNull(brokerConfig.getMessage());
        assertNotNull(brokerConfig.getTopic());
        assertNotNull(brokerConfig.getTimer());
        assertNotNull(brokerConfig.getTransaction());
        assertNotNull(brokerConfig.getTask());
        assertNotNull(brokerConfig.getGrpc());
        assertNotNull(brokerConfig.getRpcServer());
        assertNotNull(brokerConfig.getRpcClient());
    }

    @Test
    void testLoadCreatesValidConfig() {
        ConfigLoader.load(context);

        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class);

        assertNotNull(brokerConfig.getGrpc());
        assertTrue(brokerConfig.getGrpc().getPort() > 0);
        assertNotNull(brokerConfig.getTopic());
        assertTrue(brokerConfig.getTopic().getReviveQueueNum() > 0);
    }
}
