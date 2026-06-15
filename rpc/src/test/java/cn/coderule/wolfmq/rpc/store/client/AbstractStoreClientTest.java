package cn.coderule.wolfmq.rpc.store.client;

import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AbstractStoreClientTest {

    private RpcClient rpcClient;
    private TestStoreClient client;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        client = new TestStoreClient(rpcClient, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetAddressAndRpcClient() {
        assertEquals("127.0.0.1:10911", client.getAddress());
        assertEquals(rpcClient, client.getRpcClient());
    }

    private static class TestStoreClient extends AbstractStoreClient {
        public TestStoreClient(RpcClient rpcClient, String address) {
            super(rpcClient, address);
        }
    }
}