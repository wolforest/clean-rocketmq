package cn.coderule.wolfmq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.ClientType;
import apache.rocketmq.v2.Settings;
import cn.coderule.wolfmq.domain.config.network.GrpcConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettingManagerTest {

    @Mock
    private GrpcConfig grpcConfig;

    private SettingManager settingManager;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(grpcConfig.getBatchConsumerPollSize()).thenReturn(32);
        when(grpcConfig.getMaxConsumerPollTime()).thenReturn(30000);
        when(grpcConfig.getProducerBackoffMillis()).thenReturn(10);
        when(grpcConfig.getProducerMaxBackoffMillis()).thenReturn(1000);
        when(grpcConfig.getProducerBackoffMultiplier()).thenReturn(2);
        when(grpcConfig.getProducerMaxAttempts()).thenReturn(3);
        when(grpcConfig.isEnableMessageTypeCheck()).thenReturn(true);
        when(grpcConfig.getMaxMessageSize()).thenReturn(4194304);
        
        settingManager = new SettingManager(grpcConfig);
    }

    @Test
    void testConstructor() {
        assertNotNull(settingManager);
        assertEquals("SettingManager", settingManager.getServiceName());
    }

    @Test
    void testUpdateAndGetSettings() {
        String clientId = "client1";
        Settings settings = Settings.newBuilder()
            .setClientType(ClientType.PRODUCER)
            .build();

        settingManager.updateSettings(clientId, settings);
        Settings retrieved = settingManager.getSettings(clientId);

        assertNotNull(retrieved);
        assertEquals(ClientType.PRODUCER, retrieved.getClientType());
    }

    @Test
    void testGetSettingsNotFound() {
        Settings settings = settingManager.getSettings("nonexistent");
        assertNull(settings);
    }

    @Test
    void testRemoveSettings() {
        String clientId = "client1";
        Settings settings = Settings.newBuilder().build();
        settingManager.updateSettings(clientId, settings);

        Settings removed = settingManager.removeSettings(clientId);
        assertNotNull(removed);
        assertNull(settingManager.getSettings(clientId));
    }

    @Test
    void testRemoveSettingsNotFound() {
        Settings removed = settingManager.removeSettings("nonexistent");
        assertNull(removed);
    }
}
