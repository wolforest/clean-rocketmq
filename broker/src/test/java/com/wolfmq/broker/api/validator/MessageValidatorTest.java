package com.wolfmq.broker.api.validator;

import com.wolfmq.domain.config.business.MessageConfig;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.business.TopicConfig;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.message.property.MessageConst;
import com.wolfmq.domain.core.exception.InvalidRequestException;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.test.MessageMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageValidatorTest {

    private MessageValidator validator;
    private BrokerConfig brokerConfig;

    @BeforeEach
    void setUp() {
        brokerConfig = new BrokerConfig();
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setMaxBodySize(1024 * 1024);
        messageConfig.setMaxPropertySize(8192);
        messageConfig.setMaxPropertyCount(128);
        messageConfig.setMaxMessageGroupSize(256);
        brokerConfig.setMessage(messageConfig);
        brokerConfig.setTimer(new TimerConfig());
        brokerConfig.setTopic(new TopicConfig());

        validator = new MessageValidator(brokerConfig);
    }

    @Test
    void testValidateValidMessage() {
        MessageBO message = MessageMock.createMessage(128);
        message.setTopic("TestTopic");
        message.setTags("test_tag");
        assertDoesNotThrow(() -> validator.validate(message));
    }

    @Test
    void testValidateTagWithPipe() {
        assertThrows(InvalidRequestException.class, () -> validator.validateTag("tag|sub"));
    }

    @Test
    void testValidateBlankTag() {
        assertThrows(InvalidRequestException.class, () -> validator.validateTag("   "));
    }

    @Test
    void testValidateEmptyTag() {
        assertDoesNotThrow(() -> validator.validateTag(""));
    }

    @Test
    void testValidateNullTag() {
        assertDoesNotThrow(() -> validator.validateTag(null));
    }

    @Test
    void testValidateValidTag() {
        assertDoesNotThrow(() -> validator.validateTag("valid_tag"));
    }

    @Test
    void testValidateTagWithControlChar() {
        assertThrows(InvalidRequestException.class, () -> validator.validateTag("tag\tname"));
    }

    @Test
    void testValidateBodyTooLarge() {
        brokerConfig.getMessage().setMaxBodySize(100);

        MessageBO message = MessageMock.createMessage(200);
        message.setTopic("TestTopic");
        assertThrows(InvalidRequestException.class, () -> validator.validate(message));
    }

    @Test
    void testValidateShardingKeyBlank() {
        MessageBO message = MessageMock.createMessage(128);
        message.setTopic("TestTopic");
        message.putProperty(MessageConst.PROPERTY_SHARDING_KEY, "   ");
        assertThrows(InvalidRequestException.class, () -> validator.validate(message));
    }

    @Test
    void testValidateShardingKeyTooLong() {
        MessageBO message = MessageMock.createMessage(128);
        message.setTopic("TestTopic");
        message.putProperty(MessageConst.PROPERTY_SHARDING_KEY, "a".repeat(256));
        assertThrows(InvalidRequestException.class, () -> validator.validate(message));
    }
}
