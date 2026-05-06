package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.config.business.TransactionConfig;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommitBufferTest {

    private CommitBuffer commitBuffer;

    @BeforeEach
    void setUp() {
        TransactionConfig config = new TransactionConfig();
        commitBuffer = new CommitBuffer(config);
    }

    @Test
    void initOffsetQueue_createsNew() {
        OffsetQueue queue = commitBuffer.initOffsetQueue(0);
        assertNotNull(queue);
    }

    @Test
    void initOffsetQueue_returnsSameQueueForSameId() {
        OffsetQueue queue1 = commitBuffer.initOffsetQueue(0);
        OffsetQueue queue2 = commitBuffer.initOffsetQueue(0);
        assertSame(queue1, queue2);
    }

    @Test
    void initOffsetQueue_differentIds() {
        OffsetQueue queue0 = commitBuffer.initOffsetQueue(0);
        OffsetQueue queue1 = commitBuffer.initOffsetQueue(1);
        assertNotSame(queue0, queue1);
    }

    @Test
    void initOperationQueue_createsNew() {
        commitBuffer.setStoreGroup("myStoreGroup");
        MessageQueue mq = commitBuffer.initOperationQueue(0);
        assertNotNull(mq);
        assertEquals("RMQ_SYS_TRANS_OP_HALF_TOPIC", mq.getTopicName());
        assertEquals("myStoreGroup", mq.getGroupName());
        assertEquals(0, mq.getQueueId());
    }

    @Test
    void initOperationQueue_returnsSameForSameId() {
        commitBuffer.setStoreGroup("myStoreGroup");
        MessageQueue mq1 = commitBuffer.initOperationQueue(0);
        MessageQueue mq2 = commitBuffer.initOperationQueue(0);
        assertSame(mq1, mq2);
    }

    @Test
    void getOffsetEntrySet_initiallyEmpty() {
        assertTrue(commitBuffer.getOffsetEntrySet().isEmpty());
    }

    @Test
    void getOffsetEntrySet_afterInit() {
        commitBuffer.initOffsetQueue(0);
        commitBuffer.initOffsetQueue(1);
        assertEquals(2, commitBuffer.getOffsetEntrySet().size());
    }
}