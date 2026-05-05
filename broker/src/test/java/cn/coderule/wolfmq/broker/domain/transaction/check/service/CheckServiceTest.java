package cn.coderule.wolfmq.broker.domain.transaction.check.service;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.broker.domain.transaction.receipt.ReceiptRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CheckServiceTest {

    @Test
    void testLifecycleDoesNotThrow() {
        TransactionConfig config = mock(TransactionConfig.class);
        when(config.getCheckThreadNum()).thenReturn(1);
        when(config.getMaxCheckThreadNum()).thenReturn(2);
        when(config.getKeepAliveTime()).thenReturn(60);
        when(config.getCheckQueueCapacity()).thenReturn(100);

        ReceiptRegistry registry = mock(ReceiptRegistry.class);
        CheckService service = new CheckService(config, registry);

        assertDoesNotThrow(() -> service.start());
        assertDoesNotThrow(() -> service.shutdown());
    }
}