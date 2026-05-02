package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviverFactoryTest {

    @Test
    void testConstructor() {
        ReviveContext context = mock(ReviveContext.class);
        RetryService retryService = mock(RetryService.class);
        ReviverFactory factory = new ReviverFactory(context, retryService);
        assertNotNull(factory);
    }
}