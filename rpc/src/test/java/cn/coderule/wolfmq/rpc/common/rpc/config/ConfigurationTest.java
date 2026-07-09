package cn.coderule.wolfmq.rpc.common.rpc.config;

import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {

    @Test
    void constructor_WithNoArgs_ShouldInitialize() {
        Configuration config = new Configuration();
        assertNotNull(config);
    }

    @Test
    void constructor_WithNull_ShouldNotThrow() {
        Configuration config = new Configuration((Object[]) null);
        assertNotNull(config);
    }

    @Test
    void constructor_WithConfigObject_ShouldRegister() {
        RpcServerConfig serverConfig = new RpcServerConfig();
        Configuration config = new Configuration(serverConfig);
        assertNotNull(config);
    }

    @Test
    void registerConfig_ShouldNotThrow() {
        Configuration config = new Configuration();
        RpcServerConfig serverConfig = new RpcServerConfig();
        assertDoesNotThrow(() -> config.registerConfig(serverConfig));
    }

    @Test
    void registerConfig_WithNull_ShouldNotThrow() {
        Configuration config = new Configuration();
        assertDoesNotThrow(() -> config.registerConfig(null));
    }
}