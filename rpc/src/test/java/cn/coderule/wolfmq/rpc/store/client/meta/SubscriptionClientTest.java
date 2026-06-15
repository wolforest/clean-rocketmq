package cn.coderule.wolfmq.rpc.store.client.meta;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionClientTest {

    private RpcClient rpcClient;
    private SubscriptionClient client;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        client = new SubscriptionClient(rpcClient, "127.0.0.1:10911");
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
    void existsGroup_ShouldReturnFalse() {
        assertFalse(client.existsGroup("t1", "g1"));
    }

    @Test
    void getGroup_ShouldReturnNull() {
        assertNull(client.getGroup("t1", "g1"));
    }

    @Test
    void getGroupAsync_ShouldReturnNull() {
        assertNull(client.getGroupAsync("t1", "g1"));
    }

    @Test
    void putGroup_ShouldNotThrow() {
        SubscriptionRequest request = SubscriptionRequest.builder().build();
        assertDoesNotThrow(() -> client.putGroup(request));
    }

    @Test
    void saveGroup_ShouldNotThrow() {
        SubscriptionRequest request = SubscriptionRequest.builder().build();
        assertDoesNotThrow(() -> client.saveGroup(request));
    }

    @Test
    void deleteGroup_ShouldNotThrow() {
        SubscriptionRequest request = SubscriptionRequest.builder().build();
        assertDoesNotThrow(() -> client.deleteGroup(request));
    }
}