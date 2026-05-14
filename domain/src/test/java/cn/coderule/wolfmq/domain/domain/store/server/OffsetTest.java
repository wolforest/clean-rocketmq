package cn.coderule.wolfmq.domain.domain.store.server;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

class OffsetTest {

    @Test
    void testDefaultValues() {
        Offset offset = new Offset();
        
        assertEquals(-1L, offset.getCommitLogOffset());
        assertEquals(-1L, offset.getDispatchedOffset());
        assertEquals(-1L, offset.getIndexOffset());
        assertNotNull(offset.getCommitOffsetMap());
        assertNotNull(offset.getDispatchedOffsetMap());
    }

    @Test
    void testSetters() {
        Offset offset = new Offset();
        
        offset.setCommitLogOffset(100L);
        assertEquals(100L, offset.getCommitLogOffset());
        
        offset.setDispatchedOffset(200L);
        assertEquals(200L, offset.getDispatchedOffset());
        
        offset.setIndexOffset(300L);
        assertEquals(300L, offset.getIndexOffset());
    }

    @Test
    void testCommitOffsetMap() {
        Offset offset = new Offset();
        
        offset.getCommitOffsetMap().put(0, 100L);
        offset.getCommitOffsetMap().put(1, 200L);
        
        assertEquals(100L, offset.getCommitOffsetMap().get(0));
        assertEquals(200L, offset.getCommitOffsetMap().get(1));
    }
}