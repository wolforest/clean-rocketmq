package com.wolfmq.broker.domain.timer.transit;

import com.wolfmq.broker.domain.timer.context.TimerContext;
import com.wolfmq.broker.infra.store.MQStore;
import com.wolfmq.common.config.server.BrokerConfig;
import com.wolfmq.common.server.task.QueueTask;
import com.wolfmq.common.domain.timer.model.TimerQueue;
import com.wolfmq.common.domain.timer.state.TimerState;
import com.wolfmq.common.test.ConfigMock;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerQueueConsumerTest {

    @Test
    void getServiceName_ShouldReturnClassName() {
        BrokerConfig brokerConfig = ConfigMock.createBrokerConfig();
        TimerContext context = TimerContext.builder()
            .brokerConfig(brokerConfig)
            .timerState(mock(TimerState.class))
            .timerQueue(mock(TimerQueue.class))
            .mqStore(mock(MQStore.class))
            .build();
        QueueTask task = mock(QueueTask.class);

        TimerQueueConsumer consumer = new TimerQueueConsumer(context, task);
        assertEquals("TimerQueueConsumer", consumer.getServiceName());
    }
}
