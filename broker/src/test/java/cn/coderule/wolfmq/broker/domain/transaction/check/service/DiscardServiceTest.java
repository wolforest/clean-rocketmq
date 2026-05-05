package cn.coderule.wolfmq.broker.domain.transaction.check.service;

import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
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