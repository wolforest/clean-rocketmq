package cn.coderule.wolfmq.broker.api.validator;

import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GroupValidatorTest {

    @Test
    void testValidateValidGroup() {
        assertDoesNotThrow(() -> GroupValidator.validate("test_group"));
    }

    @Test
    void testValidateAlphanumericGroup() {
        assertDoesNotThrow(() -> GroupValidator.validate("group123"));
    }

    @Test
    void testValidateGroupWithUnderscore() {
        assertDoesNotThrow(() -> GroupValidator.validate("my_group"));
    }

    @Test
    void testValidateGroupWithDash() {
        assertDoesNotThrow(() -> GroupValidator.validate("my-group"));
    }

    @Test
    void testValidateGroupWithPercent() {
        assertDoesNotThrow(() -> GroupValidator.validate("group%name"));
    }

    @Test
    void testValidateGroupWithPipe() {
        assertDoesNotThrow(() -> GroupValidator.validate("group|name"));
    }

    @Test
    void testValidateBlankGroup() {
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate(""));
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("   "));
    }

    @Test
    void testValidateNullGroup() {
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate(null));
    }

    @Test
    void testValidateTooLongGroup() {
        String longGroup = "a".repeat(256);
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate(longGroup));
    }

    @Test
    void testValidateMaxLengthGroup() {
        String maxGroup = "a".repeat(255);
        assertDoesNotThrow(() -> GroupValidator.validate(maxGroup));
    }

    @Test
    void testValidateGroupWithSpecialChars() {
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("group@name"));
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("group.name"));
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("group name"));
    }

    @Test
    void testValidateSystemConsumerGroup() {
        assertThrows(InvalidParameterException.class, () -> GroupValidator.validate("CID_RMQ_SYS_Consumer"));
    }
}