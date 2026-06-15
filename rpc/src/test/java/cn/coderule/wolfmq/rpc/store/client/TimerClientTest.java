package cn.coderule.wolfmq.rpc.store.client;

import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerClientTest {

    private RpcClient rpcClient;
    private TimerClient timerClient;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        timerClient = new TimerClient(rpcClient, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetAddress() {
        assertEquals("127.0.0.1:10911", timerClient.getAddress());
    }

    @Test
    void constructor_ShouldSetRpcClient() {
        assertEquals(rpcClient, timerClient.getRpcClient());
    }
}