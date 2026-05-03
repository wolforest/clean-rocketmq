package cn.coderule.wolfmq.broker.domain.transaction.receipt;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReceiptCleanerTest {

    @Test
    void testGetServiceName() {
        TransactionConfig config = mock(TransactionConfig.class);
        ReceiptRegistry registry = mock(ReceiptRegistry.class);
        ReceiptCleaner cleaner = new ReceiptCleaner(config, registry);
        assertEquals("ReceiptCleaner", cleaner.getServiceName());
    }
}