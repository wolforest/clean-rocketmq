package cn.coderule.wolfmq.broker.domain.consumer.revive;

import cn.coderule.wolfmq.domain.config.business.MessageConfig;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReviveConsumerTest {

    @Mock
    private ReviveContext reviveContext;

    @Mock
    private MessageConfig messageConfig;

    @Mock
    private MQFacade mqFacade;

    private ReviveConsumer reviveConsumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(reviveContext.getMessageConfig()).thenReturn(messageConfig);
        when(reviveContext.getReviveTopic()).thenReturn("%RETRY%TestTopic");
        when(reviveContext.getMqFacade()).thenReturn(mqFacade);
        when(messageConfig.getReviveScanTime()).thenReturn(10000L);
        when(messageConfig.isEnablePopLog()).thenReturn(false);
        when(messageConfig.isEnableSkipLongAwaitingAck()).thenReturn(true);
        when(messageConfig.getReviveAckWaitMs()).thenReturn(30000L);

        reviveConsumer = new ReviveConsumer(reviveContext, 0);
    }

    @Test
    void testConstructor() {
        assertNotNull(reviveConsumer);
    }

    @Test
    void testSetSkipRevive() {
        // Should not throw when setting skip revive
        reviveConsumer.setSkipRevive(true);
        assertTrue(true); // Just verify no exception
    }

    @Test
    void testConsume() {
        when(mqFacade.get(any())).thenReturn(
            cn.coderule.wolfmq.domain.domain.store.domain.mq.DequeueResult.builder()
                .messageList(java.util.Collections.emptyList())
                .build()
        );

        cn.coderule.wolfmq.domain.domain.consumer.revive.ReviveBuffer buffer = reviveConsumer.consume(0);

        assertNotNull(buffer);
    }

    @Test
    void testConsumeWithSkipRevive() {
        reviveConsumer.setSkipRevive(true);

        cn.coderule.wolfmq.domain.domain.consumer.revive.ReviveBuffer buffer = reviveConsumer.consume(0);

        assertNotNull(buffer);
        // Should return empty buffer immediately when skipRevive is true
    }
}
