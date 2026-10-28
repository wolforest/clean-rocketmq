package com.wolfmq.rpc.store.client.meta;

import com.wolfmq.rpc.common.rpc.RpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOffsetClientTest {

    private RpcClient rpcClient;
    private ConsumeOffsetClient client;

    @BeforeEach
    void setUp() {
        rpcClient = mock(RpcClient.class);
        client = new ConsumeOffsetClient(rpcClient, "127.0.0.1:10911");
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
    void getOffset_ShouldReturnNull() {
        assertNull(client.getOffset(null));
    }

    @Test
    void getAndRemove_ShouldReturnNull() {
        assertNull(client.getAndRemove(null));
    }

    @Test
    void findTopicByGroup_ShouldReturnNull() {
        assertNull(client.findTopicByGroup(null));
    }

    @Test
    void findGroupByTopic_ShouldReturnNull() {
        assertNull(client.findGroupByTopic(null));
    }
}