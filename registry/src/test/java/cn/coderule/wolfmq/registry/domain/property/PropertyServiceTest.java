package cn.coderule.wolfmq.registry.domain.property;

import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.wolfmq.domain.config.server.RegistryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PropertyServiceTest {

    private RegistryConfig registryConfig;
    private RpcServerConfig rpcServerConfig;
    private PropertyService propertyService;

    @BeforeEach
    void setUp() {
        registryConfig = mock(RegistryConfig.class);
        when(registryConfig.getConfigBlackList()).thenReturn("");
        rpcServerConfig = mock(RpcServerConfig.class);
        propertyService = new PropertyService(registryConfig, rpcServerConfig);
    }

    @Test
    void testValidateBlackListWithBlacklistedKey() {
        Properties props = new Properties();
        props.setProperty("configBlackList", "value");
        assertTrue(propertyService.validateBlackList(props));
    }

    @Test
    void testValidateBlackListWithNonBlacklistedKey() {
        Properties props = new Properties();
        props.setProperty("maxMessageSize", "1000");
        assertFalse(propertyService.validateBlackList(props));
    }

    @Test
    void testValidateBlackListEmptyProperties() {
        Properties props = new Properties();
        assertFalse(propertyService.validateBlackList(props));
    }

    @Test
    void testUpdate() {
        Properties props = new Properties();
        assertDoesNotThrow(() -> propertyService.update(props));
    }

    @Test
    void testGetString() {
        String result = propertyService.getString();
        assertNotNull(result);
    }
}