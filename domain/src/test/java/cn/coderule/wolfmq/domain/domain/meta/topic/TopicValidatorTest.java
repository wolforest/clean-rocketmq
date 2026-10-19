package cn.coderule.wolfmq.domain.domain.meta.topic;

import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicValidatorTest {

    @Test
    void isTopicOrGroupIllegal_validChars() {
        assertFalse(TopicValidator.isTopicOrGroupIllegal("myTopic"));
        assertFalse(TopicValidator.isTopicOrGroupIllegal("topic_123"));
        assertFalse(TopicValidator.isTopicOrGroupIllegal("TOPIC-NAME"));
        assertFalse(TopicValidator.isTopicOrGroupIllegal("topic%name"));
        assertFalse(TopicValidator.isTopicOrGroupIllegal("topic|name"));
    }

    @Test
    void isTopicOrGroupIllegal_invalidChars() {
        assertTrue(TopicValidator.isTopicOrGroupIllegal("topic name"));
        assertTrue(TopicValidator.isTopicOrGroupIllegal("topic@name"));
        assertTrue(TopicValidator.isTopicOrGroupIllegal("topic#name"));
        assertTrue(TopicValidator.isTopicOrGroupIllegal("中文"));
    }

    @Test
    void validateTopic_valid() {
        assertDoesNotThrow(() -> TopicValidator.validateTopic("myTopic"));
    }

    @Test
    void validateTopic_blankThrows() {
        assertThrows(InvalidParameterException.class, () -> TopicValidator.validateTopic(""));
        assertThrows(InvalidParameterException.class, () -> TopicValidator.validateTopic(null));
    }

    @Test
    void validateTopic_tooLongThrows() {
        assertThrows(InvalidParameterException.class, () -> TopicValidator.validateTopic("a".repeat(128)));
    }

    @Test
    void validateTopic_illegalCharsThrows() {
        assertThrows(InvalidParameterException.class, () -> TopicValidator.validateTopic("my topic"));
    }

    @Test
    void validateAndReturn_valid() {
        TopicValidator.ValidateTopicResult result = TopicValidator.validateAndReturn("myTopic");
        assertTrue(result.isValid());
        assertEquals("", result.getRemark());
    }

    @Test
    void validateAndReturn_blank() {
        TopicValidator.ValidateTopicResult result = TopicValidator.validateAndReturn("");
        assertFalse(result.isValid());
    }

    @Test
    void validateAndReturn_illegalChars() {
        TopicValidator.ValidateTopicResult result = TopicValidator.validateAndReturn("my@topic");
        assertFalse(result.isValid());
    }

    @Test
    void validateAndReturn_tooLong() {
        TopicValidator.ValidateTopicResult result = TopicValidator.validateAndReturn("a".repeat(128));
        assertFalse(result.isValid());
    }

    @Test
    void isSystemTopic_knownSystemTopics() {
        assertTrue(TopicValidator.isSystemTopic("TBW102"));
        assertTrue(TopicValidator.isSystemTopic("SCHEDULE_TOPIC_XXXX"));
        assertTrue(TopicValidator.isSystemTopic("RMQ_SYS_TRANS_HALF_TOPIC"));
        assertTrue(TopicValidator.isSystemTopic("rmq_sys_custom"));
    }

    @Test
    void isSystemTopic_normalTopic() {
        assertFalse(TopicValidator.isSystemTopic("myTopic"));
    }

    @Test
    void isNotAllowedSendTopic() {
        assertTrue(TopicValidator.isNotAllowedSendTopic("SCHEDULE_TOPIC_XXXX"));
        assertTrue(TopicValidator.isNotAllowedSendTopic("RMQ_SYS_TRANS_HALF_TOPIC"));
        assertFalse(TopicValidator.isNotAllowedSendTopic("myTopic"));
    }
}
