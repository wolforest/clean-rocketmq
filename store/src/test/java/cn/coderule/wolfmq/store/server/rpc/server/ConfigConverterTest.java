package cn.coderule.wolfmq.store.server.rpc.server;

import cn.coderule.wolfmq.domain.config.network.RpcServerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigConverterTest {

    @Test
    void testToServerConfig() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setHost("localhost");
        storeConfig.setPort(8080);
        storeConfig.setBossThreadNum(4);
        storeConfig.setWorkerThreadNum(8);
        storeConfig.setBusinessThreadNum(16);
        storeConfig.setCallbackThreadNum(32);

        RpcServerConfig serverConfig = ConfigConverter.toServerConfig(storeConfig);

        assertNotNull(serverConfig);
        assertEquals("localhost", serverConfig.getAddress());
        assertEquals(8080, serverConfig.getPort());
        assertEquals(4, serverConfig.getBossThreadNum());
        assertEquals(8, serverConfig.getWorkerThreadNum());
        assertEquals(16, serverConfig.getBusinessThreadNum());
        assertEquals(32, serverConfig.getCallbackThreadNum());
    }

    @Test
    void testToServerConfigWithDefaultValues() {
        StoreConfig storeConfig = new StoreConfig();
        storeConfig.setPort(8080);

        RpcServerConfig serverConfig = ConfigConverter.toServerConfig(storeConfig);

        assertNotNull(serverConfig);
        assertNotNull(serverConfig.getAddress());
        assertEquals(8080, serverConfig.getPort());
    }
}
