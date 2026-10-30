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
        assertNotNull(brokerConfig.getMessageConfig());
        assertNotNull(brokerConfig.getTopicConfig());
        assertNotNull(brokerConfig.getTimerConfig());
        assertNotNull(brokerConfig.getTransactionConfig());
        assertNotNull(brokerConfig.getTaskConfig());
        assertNotNull(brokerConfig.getGrpcConfig());
        assertNotNull(brokerConfig.getRpcServerConfig());
        assertNotNull(brokerConfig.getRpcClientConfig());
    }

    @Test
    void testLoadCreatesValidConfig() {
        ConfigLoader.load(context);

        BrokerConfig brokerConfig = context.getBean(BrokerConfig.class);

        assertNotNull(brokerConfig.getGrpcConfig());
        assertTrue(brokerConfig.getGrpcConfig().getPort() > 0);
        assertNotNull(brokerConfig.getTopicConfig());
        assertTrue(brokerConfig.getTopicConfig().getReviveQueueNum() > 0);
    }
}
