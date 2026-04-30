package cn.coderule.wolfmq.broker.server.bootstrap;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigLoaderTest {

    @Test
    void testLoad() {
        // Load the configuration
        ConfigLoader.load();
        
        // Verify the config was registered
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
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
        ConfigLoader.load();
        
        BrokerConfig brokerConfig = BrokerContext.getBean(BrokerConfig.class);
        
        // Verify all sub-configs are properly initialized
        assertNotNull(brokerConfig.getGrpcConfig());
        assertTrue(brokerConfig.getGrpcConfig().getPort() > 0);
        assertNotNull(brokerConfig.getTopicConfig());
        assertTrue(brokerConfig.getTopicConfig().getReviveQueueNum() > 0);
    }
}
