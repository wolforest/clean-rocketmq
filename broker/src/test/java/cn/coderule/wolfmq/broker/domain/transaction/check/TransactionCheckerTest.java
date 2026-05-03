package cn.coderule.wolfmq.broker.domain.transaction.check;

import cn.coderule.wolfmq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.wolfmq.broker.domain.transaction.service.TransactionMessageService;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.QueueTask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransactionCheckerTest {

    @Test
    void testGetServiceName() {
        TransactionContext context = mock(TransactionContext.class);
        BrokerConfig brokerConfig = mock(BrokerConfig.class);
        when(context.getBrokerConfig()).thenReturn(brokerConfig);
        when(brokerConfig.getTransactionConfig()).thenReturn(null);

        TransactionMessageService messageService = mock(TransactionMessageService.class);
        when(context.getMessageService()).thenReturn(messageService);

        QueueTask task = mock(QueueTask.class);
        TransactionChecker checker = new TransactionChecker(context, task);
        assertEquals("TransactionChecker", checker.getServiceName());
    }
}