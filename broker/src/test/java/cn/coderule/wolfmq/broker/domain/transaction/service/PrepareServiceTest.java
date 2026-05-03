package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.wolfmq.broker.infra.store.MQStore;
import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PrepareServiceTest {

    @Test
    void testPrepare() {
        TransactionConfig config = mock(TransactionConfig.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        MQStore mqStore = mock(MQStore.class);
        ReceiptRegistry receiptRegistry = mock(ReceiptRegistry.class);
        PrepareService service = new PrepareService(config, messageFactory, mqStore, receiptRegistry);

        RequestContext context = mock(RequestContext.class);
        MessageBO messageBO = mock(MessageBO.class);
        MessageBO prepareMessage = mock(MessageBO.class);
        when(prepareMessage.getRealTopic()).thenReturn("topic");
        when(prepareMessage.getStoreGroup()).thenReturn(null);
        when(prepareMessage.getMessageId()).thenReturn(null);
        when(prepareMessage.getTransactionId()).thenReturn(null);
        when(prepareMessage.getCommitOffset()).thenReturn(0L);
        when(prepareMessage.getQueueOffset()).thenReturn(0L);
        when(messageFactory.createPrepareMessage(messageBO)).thenReturn(prepareMessage);

        EnqueueResult enqueueResult = mock(EnqueueResult.class);
        when(enqueueResult.isSuccess()).thenReturn(true);
        when(mqStore.enqueueAsync(any(EnqueueRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(enqueueResult));

        CompletableFuture<EnqueueResult> result = service.prepare(context, messageBO);
        assertNotNull(result);
        verify(messageFactory).createPrepareMessage(messageBO);
    }
}