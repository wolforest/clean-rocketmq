package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedTimerStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteTimerStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerStoreTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedTimerStore embedStore;

    @Mock
    private RemoteTimerStore remoteStore;

    private TimerStore timerStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        timerStore = new TimerStore(brokerConfig, embedStore, remoteStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(timerStore);
    }

    @Test
    void testStoreCheckpoint_embedGroup() {
        TimerCheckpoint checkpoint = mock(TimerCheckpoint.class);
        when(checkpoint.getStoreGroup()).thenReturn("embedGroup");
        when(embedStore.isClusterGroup("embedGroup")).thenReturn(true);

        timerStore.storeCheckpoint(checkpoint);

        verify(embedStore).storeCheckpoint(checkpoint);
        verify(remoteStore, never()).storeCheckpoint(any());
    }

    @Test
    void testStoreCheckpoint_remoteDisabled() {
        TimerCheckpoint checkpoint = mock(TimerCheckpoint.class);
        when(checkpoint.getStoreGroup()).thenReturn("remoteGroup");
        when(embedStore.isClusterGroup("remoteGroup")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        timerStore.storeCheckpoint(checkpoint);

        verify(embedStore, never()).storeCheckpoint(any());
        verify(remoteStore, never()).storeCheckpoint(any());
    }

    @Test
    void testStoreCheckpoint_remoteGroup() {
        TimerCheckpoint checkpoint = mock(TimerCheckpoint.class);
        when(checkpoint.getStoreGroup()).thenReturn("remoteGroup");
        when(embedStore.isClusterGroup("remoteGroup")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);

        timerStore.storeCheckpoint(checkpoint);

        verify(remoteStore).storeCheckpoint(checkpoint);
    }
}