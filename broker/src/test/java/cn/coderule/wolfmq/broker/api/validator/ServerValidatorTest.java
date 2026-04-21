package cn.coderule.wolfmq.broker.api.validator;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.core.exception.InvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServerValidatorTest {

    private BrokerConfig brokerConfig;
    private ServerValidator validator;

    @BeforeEach
    void setUp() {
        brokerConfig = new BrokerConfig();
        validator = new ServerValidator(brokerConfig);
    }

    @Test
    void testCheckServerReady_WhenReadyTimeIsZero() {
        brokerConfig.setServerReadyTime(0);
        assertDoesNotThrow(() -> validator.checkServerReady());
    }

    @Test
    void testCheckServerReady_WhenReadyTimeIsNegative() {
        brokerConfig.setServerReadyTime(-1);
        assertDoesNotThrow(() -> validator.checkServerReady());
    }

    @Test
    void testCheckServerReady_WhenReadyTimeIsPast() {
        brokerConfig.setServerReadyTime(System.currentTimeMillis() - 10000);
        assertDoesNotThrow(() -> validator.checkServerReady());
    }

    @Test
    void testCheckServerReady_WhenReadyTimeIsFuture() {
        brokerConfig.setServerReadyTime(System.currentTimeMillis() + 60000);
        assertThrows(InvalidParameterException.class, () -> validator.checkServerReady());
    }
}
