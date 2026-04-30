package cn.coderule.wolfmq.broker.domain.timer.context;

import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.QueueTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

class TimerContextTest {

    private TimerContext timerContext;

    @BeforeEach
    void setUp() {
        timerContext = new TimerContext();
    }

    @Test
    void testBuilder() {
        BrokerConfig brokerConfig = new BrokerConfig();

        TimerContext context = TimerContext.builder()
            .brokerConfig(brokerConfig)
            .build();

        assertNotNull(context);
        assertEquals(brokerConfig, context.getBrokerConfig());
    }

    @Test
    void testDefaultConstructor() {
        TimerContext context = new TimerContext();
        assertNotNull(context);
    }

    @Test
    void testInitQueueTask() {
        QueueTask task = new QueueTask("testGroup", 0);
        timerContext.initQueueTask(task);

        assertEquals(task, timerContext.getQueueTask());
    }

    @Test
    void testInitQueueTaskWhenAlreadySet() {
        QueueTask task1 = new QueueTask("testGroup", 0);
        QueueTask task2 = new QueueTask("testGroup", 1);

        timerContext.initQueueTask(task1);
        timerContext.initQueueTask(task2); // Should not override

        assertEquals(task1, timerContext.getQueueTask());
    }

    @Test
    void testGetOrWaitQueueTaskWithExistingTask() throws Exception {
        QueueTask task = new QueueTask("testGroup", 0);
        timerContext.initQueueTask(task);

        QueueTask retrieved = timerContext.getOrWaitQueueTask();
        assertEquals(task, retrieved);
    }

    @Test
    void testGetOrWaitQueueTaskTimeout() {
        // Should timeout when no task is set
        assertThrows(TimeoutException.class, () -> {
            timerContext.getOrWaitQueueTask();
        });
    }

    @Test
    void testSettersAndGetters() {
        BrokerConfig brokerConfig = new BrokerConfig();
        timerContext.setBrokerConfig(brokerConfig);
        assertEquals(brokerConfig, timerContext.getBrokerConfig());

        QueueTask task = new QueueTask("testGroup", 0);
        timerContext.setQueueTask(task);
        assertEquals(task, timerContext.getQueueTask());
    }
}
