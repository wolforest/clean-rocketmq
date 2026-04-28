package cn.coderule.wolfmq.broker.domain.consumer.consumer;

import cn.coderule.wolfmq.domain.domain.consumer.consume.ConsumeContext;
import cn.coderule.wolfmq.domain.domain.consumer.hook.ConsumeHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeHookManagerTest {

    private ConsumeHookManager hookManager;
    private ConsumeHook mockHook1;
    private ConsumeHook mockHook2;
    private ConsumeContext context;

    @BeforeEach
    void setUp() {
        hookManager = new ConsumeHookManager();
        mockHook1 = mock(ConsumeHook.class);
        mockHook2 = mock(ConsumeHook.class);
        context = new ConsumeContext();
        
        when(mockHook1.hookName()).thenReturn("Hook1");
        when(mockHook2.hookName()).thenReturn("Hook2");
    }

    @Test
    void testConstructor() {
        assertNotNull(hookManager);
        assertEquals("ConsumeHookManager", hookManager.hookName());
    }

    @Test
    void testRegisterHook() {
        hookManager.registerHook(mockHook1);
        List<String> hookNames = hookManager.getHookNameList();
        
        assertEquals(1, hookNames.size());
        assertEquals("Hook1", hookNames.get(0));
    }

    @Test
    void testRegisterMultipleHooks() {
        hookManager.registerHook(mockHook1);
        hookManager.registerHook(mockHook2);
        List<String> hookNames = hookManager.getHookNameList();
        
        assertEquals(2, hookNames.size());
        assertTrue(hookNames.contains("Hook1"));
        assertTrue(hookNames.contains("Hook2"));
    }

    @Test
    void testPreConsumeWithNoHooks() {
        // Should not throw when no hooks registered
        assertDoesNotThrow(() -> hookManager.preConsume(context));
    }

    @Test
    void testPreConsumeWithHooks() {
        hookManager.registerHook(mockHook1);
        hookManager.registerHook(mockHook2);
        
        hookManager.preConsume(context);
        
        verify(mockHook1).preConsume(context);
        verify(mockHook2).preConsume(context);
    }

    @Test
    void testPostConsumeWithNoHooks() {
        // Should not throw when no hooks registered
        assertDoesNotThrow(() -> hookManager.PostConsume(context));
    }

    @Test
    void testPostConsumeWithHooks() {
        hookManager.registerHook(mockHook1);
        hookManager.registerHook(mockHook2);
        
        hookManager.PostConsume(context);
        
        verify(mockHook1).PostConsume(context);
        verify(mockHook2).PostConsume(context);
    }

    @Test
    void testGetHookNameListEmpty() {
        List<String> hookNames = hookManager.getHookNameList();
        assertNotNull(hookNames);
        assertTrue(hookNames.isEmpty());
    }
}
