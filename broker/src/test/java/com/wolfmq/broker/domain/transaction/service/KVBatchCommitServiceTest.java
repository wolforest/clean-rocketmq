package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.domain.domain.transaction.CommitBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KVBatchCommitServiceTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        com.wolfmq.domain.config.business.TransactionConfig transactionConfig = new com.wolfmq.domain.config.business.TransactionConfig();
        CommitBuffer commitBuffer = mock(CommitBuffer.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        MQStore mqStore = mock(MQStore.class);

        BatchCommitService service = new BatchCommitService(transactionConfig, commitBuffer, messageFactory, mqStore);
        assertEquals("BatchCommitService", service.getServiceName());
    }
}
