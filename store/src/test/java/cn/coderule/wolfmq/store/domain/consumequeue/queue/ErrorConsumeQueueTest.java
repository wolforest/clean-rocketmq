package cn.coderule.wolfmq.store.domain.consumequeue.queue;

import cn.coderule.wolfmq.domain.core.enums.store.QueueType;
import cn.coderule.wolfmq.domain.domain.store.domain.consumequeue.QueueUnit;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorConsumeQueueTest {

    @Test
    void testSingleton() {
        ErrorConsumeQueue instance1 = ErrorConsumeQueue.singleton("test");
        ErrorConsumeQueue instance2 = ErrorConsumeQueue.singleton("test2");
        
        assertSame(instance1, instance2);
    }

    @Test
    void testGetInstance() {
        ErrorConsumeQueue instance = ErrorConsumeQueue.INSTANCE;
        
        assertNotNull(instance);
    }

    @Test
    void testGetQueueType() {
        assertEquals(QueueType.ERROR, ErrorConsumeQueue.INSTANCE.getQueueType());
    }

    @Test
    void testGetTopic() {
        assertEquals("NO_SUCH_TOPIC", ErrorConsumeQueue.INSTANCE.getTopic());
    }

    @Test
    void testGetQueueId() {
        assertEquals(0, ErrorConsumeQueue.INSTANCE.getQueueId());
    }

    @Test
    void testGetUnitSize() {
        assertEquals(0, ErrorConsumeQueue.INSTANCE.getUnitSize());
    }

    @Test
    void testEnqueueDoesNotThrow() {
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.enqueue(null));
    }

    @Test
    void testGetReturnsNull() {
        assertNull(ErrorConsumeQueue.INSTANCE.get(0));
    }

    @Test
    void testGetListReturnsEmptyList() {
        List<QueueUnit> result = ErrorConsumeQueue.INSTANCE.get(0, 10);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testOffsetMethods() {
        assertEquals(0, ErrorConsumeQueue.INSTANCE.getMinOffset());
        assertEquals(0, ErrorConsumeQueue.INSTANCE.getMaxOffset());
        assertEquals(0, ErrorConsumeQueue.INSTANCE.rollToOffset(100));
        assertEquals(0, ErrorConsumeQueue.INSTANCE.increaseOffset());
        assertEquals(0, ErrorConsumeQueue.INSTANCE.getMaxCommitOffset());
    }

    @Test
    void testSetOffsetMethodsDoNotThrow() {
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.setMinOffset(100));
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.setMaxOffset(200));
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.setMaxCommitOffset(300));
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.setCommitOffsetByShardId(0, 100L));
    }

    @Test
    void testGetCommitOffsetByShardIdReturnsNull() {
        assertNull(ErrorConsumeQueue.INSTANCE.getCommitOffsetByShardId(0));
    }

    @Test
    void testGetCommitOffsetMapReturnsEmptyMap() {
        Map<Integer, Long> map = ErrorConsumeQueue.INSTANCE.getCommitOffsetMap();
        
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void testSetCommitOffsetMapDoesNotThrow() {
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.setCommitOffsetMap(null));
    }

    @Test
    void testGetMappedFileQueueReturnsNull() {
        assertNull(ErrorConsumeQueue.INSTANCE.getMappedFileQueue());
    }

    @Test
    void testLifecycleMethodsDoNotThrow() {
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.load());
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.flush(0));
        assertDoesNotThrow(() -> ErrorConsumeQueue.INSTANCE.destroy());
    }
}