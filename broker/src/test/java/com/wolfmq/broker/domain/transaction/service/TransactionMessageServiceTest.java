package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.broker.infra.store.ConsumeOffsetStore;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.broker.infra.store.TopicStore;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.domain.domain.transaction.CommitBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionMessageServiceTest {

    @Test
    void testConstructorDoesNotThrow() {
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        when(brokerConfig.getTopicConfig()).thenReturn(null);
        when(brokerConfig.getTransactionConfig()).thenReturn(null);

        CommitBuffer commitBuffer = mock(CommitBuffer.class);
        BatchCommitService batchCommitService = mock(BatchCommitService.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        MQStore mqStore = mock(MQStore.class);
        TopicStore topicStore = mock(TopicStore.class);
        ConsumeOffsetStore consumeOffsetStore = mock(ConsumeOffsetStore.class);

        assertDoesNotThrow(() -> new TransactionMessageService(
            brokerConfig, commitBuffer, batchCommitService,
            messageFactory, mqStore, topicStore, consumeOffsetStore
        ));
    }
}