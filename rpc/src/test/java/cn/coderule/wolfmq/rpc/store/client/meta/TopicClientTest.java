package cn.coderule.wolfmq.rpc.store.client.meta;

import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopicClientTest {

    private RpcClient rpcClient;
    private TopicClient client;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        client = new TopicClient(rpcClient, "127.0.0.1:10911");
    }

    @Test
    void constructor_ShouldSetAddress() {
        assertEquals("127.0.0.1:10911", client.getAddress());
    }

    @Test
    void constructor_ShouldSetRpcClient() {
        assertEquals(rpcClient, client.getRpcClient());
    }

    @Test
    void exists_ShouldReturnFalse() {
        assertFalse(client.exists("test-topic"));
    }

    @Test
    void getTopic_ShouldReturnNull() {
        assertNull(client.getTopic("test-topic"));
    }

    @Test
    void getTopicAsync_ShouldReturnNull() {
        assertNull(client.getTopicAsync("test-topic"));
    }
}