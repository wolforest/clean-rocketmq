package cn.coderule.wolfmq.broker.domain.transaction.service;

import cn.coderule.wolfmq.broker.infra.store.MQStore;
import cn.coderule.wolfmq.domain.domain.transaction.CommitBuffer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KVBatchCommitServiceTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        cn.coderule.wolfmq.domain.config.business.TransactionConfig transactionConfig = new cn.coderule.wolfmq.domain.config.business.TransactionConfig();
        CommitBuffer commitBuffer = mock(CommitBuffer.class);
        MessageFactory messageFactory = mock(MessageFactory.class);
        MQStore mqStore = mock(MQStore.class);

        BatchCommitService service = new BatchCommitService(transactionConfig, commitBuffer, messageFactory, mqStore);
        assertEquals("BatchCommitService", service.getServiceName());
    }
}
