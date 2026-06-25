package cn.coderule.wolfmq.rpc.registry.route;

import cn.coderule.wolfmq.rpc.registry.RegistryClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteLoaderTest {

    private RegistryClient registryClient;
    private RouteLoader routeLoader;

    @BeforeEach
    void setUp() {
        registryClient = mock(RegistryClient.class);
        routeLoader = new RouteLoader(registryClient, 30000, 3000);
    }

    @Test
    void constructor_WithDefaultInterval_ShouldInitialize() {
        RouteLoader loader = new RouteLoader(registryClient);
        assertNotNull(loader);
    }

    @Test
    void constructor_WithCustomInterval_ShouldInitialize() {
        RouteLoader loader = new RouteLoader(registryClient, 60000, 5000);
        assertNotNull(loader);
    }

    @Test
    void constructor_WithTwoArgs_ShouldInitialize() {
        RouteLoader loader = new RouteLoader(registryClient, 10000);
        assertNotNull(loader);
    }
}