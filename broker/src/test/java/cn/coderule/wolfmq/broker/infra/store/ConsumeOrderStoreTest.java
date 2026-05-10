package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedConsumeOrderStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteConsumeOrderStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.meta.order.OrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumeOrderStoreTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedConsumeOrderStore embedStore;

    @Mock
    private RemoteConsumeOrderStore remoteStore;

    private ConsumeOrderStore consumeOrderStore;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumeOrderStore = new ConsumeOrderStore(brokerConfig, embedStore, remoteStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(consumeOrderStore);
    }

    @Test
    void testIsLocked_embedTopic() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.getTopicName()).thenReturn("testTopic");
        when(embedStore.containsTopic("testTopic")).thenReturn(true);
        when(embedStore.isLocked(request)).thenReturn(true);

        boolean result = consumeOrderStore.isLocked(request);

        assertTrue(result);
        verify(embedStore).isLocked(request);
    }

    @Test
    void testIsLocked_remoteTopic() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.getTopicName()).thenReturn("testTopic");
        when(embedStore.containsTopic("testTopic")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        when(remoteStore.isLocked(request)).thenReturn(false);

        boolean result = consumeOrderStore.isLocked(request);

        assertFalse(result);
        verify(remoteStore).isLocked(request);
    }

    @Test
    void testIsLocked_remoteDisabled() {
        OrderRequest request = mock(OrderRequest.class);
        when(request.getTopicName()).thenReturn("testTopic");
        when(embedStore.containsTopic("testTopic")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);

        boolean result = consumeOrderStore.isLocked(request);

        assertFalse(result);
    }
}