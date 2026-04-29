package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReviveContextTest {

    @Test
    void testBuilder() {
        BrokerConfig brokerConfig = new BrokerConfig();
        TopicConfig topicConfig = new TopicConfig();
        MessageConfig messageConfig = new MessageConfig();

        ReviveContext context = ReviveContext.builder()
            .brokerConfig(brokerConfig)
            .topicConfig(topicConfig)
            .messageConfig(messageConfig)
            .reviveTopic("%RETRY%TestTopic")
            .build();

        assertNotNull(context);
        assertEquals(brokerConfig, context.getBrokerConfig());
        assertEquals(topicConfig, context.getTopicConfig());
        assertEquals(messageConfig, context.getMessageConfig());
        assertEquals("%RETRY%TestTopic", context.getReviveTopic());
    }

    @Test
    void testDefaultConstructor() {
        ReviveContext context = new ReviveContext();
        assertNotNull(context);
    }

    @Test
    void testAllArgsConstructor() {
        BrokerConfig brokerConfig = new BrokerConfig();
        ReviveContext context = new ReviveContext(
            brokerConfig,
            new TopicConfig(),
            new MessageConfig(),
            "%RETRY%TestTopic",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        assertNotNull(context);
        assertEquals(brokerConfig, context.getBrokerConfig());
    }

    @Test
    void testSettersAndGetters() {
        ReviveContext context = new ReviveContext();
        
        BrokerConfig brokerConfig = new BrokerConfig();
        context.setBrokerConfig(brokerConfig);
        assertEquals(brokerConfig, context.getBrokerConfig());

        context.setReviveTopic("testTopic");
        assertEquals("testTopic", context.getReviveTopic());

        MessageConfig messageConfig = new MessageConfig();
        context.setMessageConfig(messageConfig);
        assertEquals(messageConfig, context.getMessageConfig());
    }
}
