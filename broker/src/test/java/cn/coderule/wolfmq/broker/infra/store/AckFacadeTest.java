package cn.coderule.wolfmq.broker.infra.store;

import cn.coderule.wolfmq.broker.infra.embed.EmbedAckStore;
import cn.coderule.wolfmq.broker.infra.remote.RemoteAckStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.checkpoint.PopCheckPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AckFacadeTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private EmbedAckStore embedAckStore;

    @Mock
    private RemoteAckStore remoteAckStore;

    private AckFacade ackFacade;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ackFacade = new AckFacade(brokerConfig, embedAckStore, remoteAckStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(ackFacade);
    }

    @Test
    void testAddCheckPoint_embedTopic() {
        PopCheckPoint point = mock(PopCheckPoint.class);
        when(point.getTopic()).thenReturn("testTopic");
        when(embedAckStore.containsTopic("testTopic")).thenReturn(true);
        
        ackFacade.addCheckPoint(point, 1, 100L, 200L);
        
        verify(embedAckStore).addCheckPoint(point, 1, 100L, 200L);
        verify(remoteAckStore, never()).addCheckPoint(any(), anyInt(), anyLong(), anyLong());
    }

    @Test
    void testAddCheckPoint_remoteTopic() {
        PopCheckPoint point = mock(PopCheckPoint.class);
        when(point.getTopic()).thenReturn("testTopic");
        when(embedAckStore.containsTopic("testTopic")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(true);
        
        ackFacade.addCheckPoint(point, 1, 100L, 200L);
        
        verify(remoteAckStore).addCheckPoint(point, 1, 100L, 200L);
    }

    @Test
    void testAddCheckPoint_remoteDisabled() {
        PopCheckPoint point = mock(PopCheckPoint.class);
        when(point.getTopic()).thenReturn("testTopic");
        when(embedAckStore.containsTopic("testTopic")).thenReturn(false);
        when(brokerConfig.isEnableRemoteStore()).thenReturn(false);
        
        ackFacade.addCheckPoint(point, 1, 100L, 200L);
        
        verify(embedAckStore, never()).addCheckPoint(any(), anyInt(), anyLong(), anyLong());
        verify(remoteAckStore, never()).addCheckPoint(any(), anyInt(), anyLong(), anyLong());
    }
}