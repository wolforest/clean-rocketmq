package cn.coderule.wolfmq.rpc.store.client.meta;

import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOrderClientTest {

    private RpcClient rpcClient;
    private ConsumeOrderClient client;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        client = new ConsumeOrderClient(rpcClient, "127.0.0.1:10911");
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
    void isLocked_ShouldReturnFalse() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();
        assertFalse(client.isLocked(request));
    }

    @Test
    void commit_ShouldReturnZero() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();
        assertEquals(0, client.commit(request));
    }

    @Test
    void lock_ShouldNotThrow() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();
        assertDoesNotThrow(() -> client.lock(request));
    }

    @Test
    void unlock_ShouldNotThrow() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();
        assertDoesNotThrow(() -> client.unlock(request));
    }

    @Test
    void updateInvisible_ShouldNotThrow() {
        OrderRequest request = OrderRequest.builder()
            .topicName("t1")
            .consumerGroup("g1")
            .queueId(0)
            .build();
        assertDoesNotThrow(() -> client.updateInvisible(request));
    }
}