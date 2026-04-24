package cn.coderule.wolfmq.registry.server.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RegistryContextTest {

    @Test
    void testApplicationContextHolder() {
        assertNotNull(RegistryContext.APPLICATION);
    }

    @Test
    void testMonitorContextHolder() {
        assertNotNull(RegistryContext.MONITOR);
    }

    @Test
    void testRegisterAndGetBean() {
        String testBean = "test-bean";
        RegistryContext.register(testBean);
        
        String retrieved = RegistryContext.getBean(String.class);
        assertEquals(testBean, retrieved);
    }

    @Test
    void testRegisterAndGetMonitor() {
        Integer monitorBean = 42;
        RegistryContext.registerMonitor(monitorBean, Integer.class);
        
        Integer retrieved = RegistryContext.getMonitor(Integer.class);
        assertEquals(42, retrieved);
    }
}