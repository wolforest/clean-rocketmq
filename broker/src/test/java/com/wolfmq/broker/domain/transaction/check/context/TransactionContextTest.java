package com.wolfmq.broker.domain.transaction.check.context;

import com.wolfmq.broker.domain.transaction.check.service.CheckService;
import com.wolfmq.broker.domain.transaction.check.service.DiscardService;
import com.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.transaction.CommitBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionContextTest {

    @Test
    void testBuilderSetsAllFields() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        CommitBuffer commitBuffer = mock(CommitBuffer.class);
        CheckService checkService = mock(CheckService.class);
        DiscardService discardService = mock(DiscardService.class);
        TransactionMessageService messageService = mock(TransactionMessageService.class);

        TransactionContext context = TransactionContext.builder()
            .brokerConfig(brokerConfig)
            .commitBuffer(commitBuffer)
            .checkService(checkService)
            .discardService(discardService)
            .messageService(messageService)
            .build();

        assertEquals(brokerConfig, context.getBrokerConfig());
        assertEquals(commitBuffer, context.getCommitBuffer());
        assertEquals(checkService, context.getCheckService());
        assertEquals(discardService, context.getDiscardService());
        assertEquals(messageService, context.getMessageService());
    }

    @Test
    void testSetters() {
        TransactionContext context = new TransactionContext();
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        context.setBrokerConfig(brokerConfig);
        assertEquals(brokerConfig, context.getBrokerConfig());
    }
}