package cn.coderule.wolfmq.broker.domain.timer;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.producer.ProduceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerHookTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private TimerConfig timerConfig;

    private TimerHook timerHook;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getTimerConfig()).thenReturn(timerConfig);
        when(timerConfig.getPrecision()).thenReturn(1000);
        when(timerConfig.getMaxDelayTime()).thenReturn(3600); // 1 hour

        timerHook = new TimerHook(brokerConfig);
    }

    @Test
    void testConstructor() {
        assertNotNull(timerHook);
        assertEquals("TimerHook", timerHook.hookName());
    }

    @Test
    void testPreProduceWithNormalMessage() {
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .build();
        message.setDeliverTime(System.currentTimeMillis() + 60000); // 1 minute later

        ProduceContext context = new ProduceContext();
        context.setMessageBO(message);

        timerHook.preProduce(context);

        // Message should be transformed to timer topic
        assertTrue(message.getTimeout() > 0);
    }

    @Test
    void testPreProduceWithTimerTopic() {
        MessageBO message = MessageBO.builder()
            .topic("rmq_sys_wheel_timer")
            .body("test".getBytes())
            .build();
        message.setDeliverTime(System.currentTimeMillis() + 60000);

        ProduceContext context = new ProduceContext();
        context.setMessageBO(message);

        // Should not transform timer topic messages
        timerHook.preProduce(context);
        // No exception means pass
    }

    @Test
    void testPreProduceWithoutTimerProperty() {
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .build();
        // No deliver time or delay time set

        ProduceContext context = new ProduceContext();
        context.setMessageBO(message);

        // Should not transform
        timerHook.preProduce(context);
        // No exception means pass
    }

    @Test
    void testPreProduceWithDelayTime() {
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .build();
        message.setDelayTime(60000); // 1 minute delay

        ProduceContext context = new ProduceContext();
        context.setMessageBO(message);

        timerHook.preProduce(context);

        // Message should be transformed
        assertTrue(message.getTimeout() > 0);
    }

    @Test
    void testPreProduceWithExceedMaxDelayTime() {
        when(timerConfig.getMaxDelayTime()).thenReturn(1); // 1 second

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .build();
        message.setDeliverTime(System.currentTimeMillis() + 7200000); // 2 hours later

        ProduceContext context = new ProduceContext();
        context.setMessageBO(message);

        assertThrows(cn.coderule.wolfmq.domain.core.exception.InvalidRequestException.class, () -> {
            timerHook.preProduce(context);
        });
    }

    @Test
    void testPostProduce() {
        ProduceContext context = new ProduceContext();
        // Should not throw
        assertDoesNotThrow(() -> timerHook.postProduce(context));
    }
}
