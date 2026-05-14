package cn.coderule.wolfmq.domain.domain.timer;

import org.junit.jupiter.api.Test;

import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScanResultTest {

    @Test
    void testEmpty() {
        ScanResult result = ScanResult.empty();
        
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddNormalMsgStack() {
        ScanResult result = ScanResult.empty();
        TimerEvent event = mock(TimerEvent.class);
        
        result.addNormalMsgStack(event);
        
        assertEquals(1, result.sizeOfNormalMsgStack());
        assertFalse(result.isEmpty());
    }

    @Test
    void testAddDeleteMsgStack() {
        ScanResult result = ScanResult.empty();
        TimerEvent event = mock(TimerEvent.class);
        
        result.addDeleteMsgStack(event);
        
        assertEquals(1, result.sizeOfDeleteMsgStack());
    }

    @Test
    void testIsSuccess() {
        ScanResult result = ScanResult.empty();
        
        assertFalse(result.isSuccess());
        
        result.setCode(1);
        assertTrue(result.isSuccess());
    }
}