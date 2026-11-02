package com.wolfmq.broker.domain.transaction.check.service;

import com.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import com.wolfmq.common.config.server.BrokerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DiscardServiceTest {

    @Test
    void testDiscardWithNullMessage() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        TransactionMessageService messageService = mock(TransactionMessageService.class);
        DiscardService service = new DiscardService(brokerConfig, messageService);

        assertDoesNotThrow(() -> service.discard(null));
    }
}
