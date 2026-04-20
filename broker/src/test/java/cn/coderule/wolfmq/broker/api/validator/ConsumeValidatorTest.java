package cn.coderule.wolfmq.broker.api.validator;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.exception.InvalidRequestException;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsumeValidatorTest {

    private BrokerConfig brokerConfig;
    private ConsumeValidator validator;

    @BeforeEach
    void setUp() {
        brokerConfig = new BrokerConfig();
        MessageConfig messageConfig = new MessageConfig();
        messageConfig.setMinInvisibleTime(10000);
        messageConfig.setMaxInvisibleTime(300000);
        messageConfig.setMaxPopSize(32);
        brokerConfig.setMessageConfig(messageConfig);

        validator = new ConsumeValidator(brokerConfig);
    }

    @Test
    void testValidatePopRequest_ValidRequest() {
        PopRequest request = PopRequest.builder()
            .consumerGroup("test_group")
            .topicName("TestTopic")
            .maxNum(16)
            .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void testValidatePopRequest_BlankGroup() {
        PopRequest request = PopRequest.builder()
            .consumerGroup("")
            .topicName("TestTopic")
            .maxNum(16)
            .build();

        assertThrows(InvalidParameterException.class, () -> validator.validate(request));
    }

    @Test
    void testValidatePopRequest_InvalidMaxNum() {
        PopRequest request = PopRequest.builder()
            .consumerGroup("test_group")
            .topicName("TestTopic")
            .maxNum(64)
            .build();

        assertThrows(InvalidRequestException.class, () -> validator.validate(request));
    }

    @Test
    void testValidateInvisibleTime_TooSmall() {
        assertThrows(InvalidRequestException.class,
            () -> validator.validateInvisibleTime(100));
    }

    @Test
    void testValidateInvisibleTime_TooLarge() {
        assertThrows(InvalidRequestException.class,
            () -> validator.validateInvisibleTime(500000));
    }

    @Test
    void testValidateInvisibleTime_Valid() {
        assertDoesNotThrow(() -> validator.validateInvisibleTime(30000));
    }

    @Test
    void testValidateAckRequest_Valid() {
        AckRequest request = AckRequest.builder()
            .groupName("test_group")
            .topicName("TestTopic")
            .build();

        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void testValidateAckRequest_BlankGroup() {
        AckRequest request = AckRequest.builder()
            .groupName("")
            .topicName("TestTopic")
            .build();

        assertThrows(InvalidParameterException.class, () -> validator.validate(request));
    }
}