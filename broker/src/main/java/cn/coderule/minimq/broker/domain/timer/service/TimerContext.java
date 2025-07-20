package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerContext implements Serializable {
    private static final int MAX_WAIT_TIMES = 100;
    private QueueTask queueTask;

    private BrokerConfig brokerConfig;
    private TimerQueue timerQueue;
    private TimerState timerState;

    private MQStore mqStore;

    public void initQueueTask(QueueTask task) {
        if (null != queueTask) {
            return;
        }

        this.queueTask = task;
    }

    public QueueTask getOrWaitQueueTask() throws InterruptedException {
        int i = 0;
        while (i < MAX_WAIT_TIMES) {
            i++;

            if (null != queueTask) {
                return queueTask;
            }

            Thread.sleep(100);
        }

        throw new InterruptedException("wait queue task timeout");
    }
}
