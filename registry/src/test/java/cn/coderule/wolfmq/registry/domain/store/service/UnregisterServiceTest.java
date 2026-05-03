package cn.coderule.wolfmq.registry.domain.store.service;

import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import cn.coderule.wolfmq.registry.domain.store.StoreRegistry;
import cn.coderule.wolfmq.rpc.registry.protocol.header.UnRegisterBrokerRequestHeader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnregisterServiceTest {

    @Test
    void testGetServiceName() {
        RegistryConfig config = mock(RegistryConfig.class);
        when(config.getUnregisterQueueCapacity()).thenReturn(100);
        StoreRegistry registry = mock(StoreRegistry.class);
        UnregisterService service = new UnregisterService(config, registry);
        assertEquals("UnregisterService", service.getServiceName());
    }

    @Test
    void testSubmit() {
        RegistryConfig config = mock(RegistryConfig.class);
        when(config.getUnregisterQueueCapacity()).thenReturn(100);
        StoreRegistry registry = mock(StoreRegistry.class);
        UnregisterService service = new UnregisterService(config, registry);

        UnRegisterBrokerRequestHeader request = mock(UnRegisterBrokerRequestHeader.class);
        assertTrue(service.submit(request));
    }

    @Test
    void testGetQueueSize() {
        RegistryConfig config = mock(RegistryConfig.class);
        when(config.getUnregisterQueueCapacity()).thenReturn(100);
        StoreRegistry registry = mock(StoreRegistry.class);
        UnregisterService service = new UnregisterService(config, registry);

        assertEquals(0, service.getQueueSize());
    }
}