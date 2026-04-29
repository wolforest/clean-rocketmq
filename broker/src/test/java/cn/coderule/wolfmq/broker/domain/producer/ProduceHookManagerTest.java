package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.domain.domain.producer.ProduceContext;
import cn.coderule.wolfmq.domain.domain.producer.hook.ProduceHook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProduceHookManagerTest {

    private ProduceHookManager hookManager;
    private ProduceHook mockHook1;
    private ProduceHook mockHook2;
    private ProduceContext context;

    @BeforeEach
    void setUp() {
        hookManager = new ProduceHookManager();
        mockHook1 = mock(ProduceHook.class);
        mockHook2 = mock(ProduceHook.class);
        context = new ProduceContext();
        
        when(mockHook1.hookName()).thenReturn("Hook1");
        when(mockHook2.hookName()).thenReturn("Hook2");
    }

    @Test
    void testConstructor() {
        assertNotNull(hookManager);
        assertEquals("ProduceHookManager", hookManager.hookName());
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
    void testPreProduceWithNoHooks() {
        // Should not throw when no hooks registered
        assertDoesNotThrow(() -> hookManager.preProduce(context));
    }

    @Test
    void testPreProduceWithHooks() {
        hookManager.registerHook(mockHook1);
        hookManager.registerHook(mockHook2);
        
        hookManager.preProduce(context);
        
        verify(mockHook1).preProduce(context);
        verify(mockHook2).preProduce(context);
    }

    @Test
    void testPostProduceWithNoHooks() {
        // Should not throw when no hooks registered
        assertDoesNotThrow(() -> hookManager.postProduce(context));
    }

    @Test
    void testPostProduceWithHooks() {
        hookManager.registerHook(mockHook1);
        hookManager.registerHook(mockHook2);
        
        hookManager.postProduce(context);
        
        verify(mockHook1).postProduce(context);
        verify(mockHook2).postProduce(context);
    }

    @Test
    void testGetHookNameListEmpty() {
        List<String> hookNames = hookManager.getHookNameList();
        assertNotNull(hookNames);
        assertTrue(hookNames.isEmpty());
    }
}
