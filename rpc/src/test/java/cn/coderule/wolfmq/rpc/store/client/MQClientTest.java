package cn.coderule.wolfmq.rpc.store.client;

import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MQClientTest {

    private RpcClient rpcClient;
    private MQClient mqClient;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        mqClient = new MQClient(rpcClient, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetAddress() {
        assertEquals("127.0.0.1:10911", mqClient.getAddress());
    }

    @Test
    void constructor_ShouldSetRpcClient() {
        assertEquals(rpcClient, mqClient.getRpcClient());
    }
}