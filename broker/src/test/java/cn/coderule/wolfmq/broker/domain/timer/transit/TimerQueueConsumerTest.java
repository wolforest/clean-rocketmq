package cn.coderule.wolfmq.broker.domain.timer.transit;

import cn.coderule.wolfmq.broker.domain.timer.context.TimerContext;
import cn.coderule.wolfmq.broker.infra.store.MQStore;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.QueueTask;
import cn.coderule.wolfmq.domain.domain.timer.TimerQueue;
import cn.coderule.wolfmq.domain.domain.timer.state.TimerState;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
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
