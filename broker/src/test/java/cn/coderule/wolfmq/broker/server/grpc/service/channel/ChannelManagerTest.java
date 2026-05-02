package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import cn.coderule.wolfmq.domain.config.network.GrpcConfig;
import cn.coderule.wolfmq.rpc.common.core.relay.response.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChannelManagerTest {

    @Mock
    private GrpcConfig grpcConfig;

    @Mock
    private SettingManager settingManager;

    private ChannelManager channelManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(grpcConfig.getRelayTimeout()).thenReturn(30000);
        channelManager = new ChannelManager(grpcConfig, settingManager);
    }

    @Test
    void testAddResult() {
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        
        String id = channelManager.addResult(future);
        
        assertNotNull(id);
        assertFalse(id.isEmpty());
    }

    @Test
    void testGetAndRemoveResult() {
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        String id = channelManager.addResult(future);
        
        CompletableFuture<Result<String>> retrieved = channelManager.getAndRemoveResult(id);
        
        assertNotNull(retrieved);
        assertSame(future, retrieved);
    }

    @Test
    void testGetAndRemoveResultReturnsNull() {
        CompletableFuture<Result<String>> retrieved = channelManager.getAndRemoveResult("nonexistent");
        assertNull(retrieved);
    }

    @Test
    void testGetAndRemoveResultRemovesFromMap() {
        CompletableFuture<Result<String>> future = new CompletableFuture<>();
        String id = channelManager.addResult(future);
        
        channelManager.getAndRemoveResult(id);
        CompletableFuture<Result<String>> secondRetrieve = channelManager.getAndRemoveResult(id);
        
        assertNull(secondRetrieve);
    }

    @Test
    void testConstructor() {
        assertNotNull(channelManager);
    }
}
