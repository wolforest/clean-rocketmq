package cn.coderule.minimq.broker.domain.timer.context;

import cn.coderule.minimq.broker.domain.timer.transit.TimerMessageProducer;
import cn.coderule.minimq.broker.domain.timer.transit.TimerTaskScheduler;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimerContext implements Serializable {
    private static final int MAX_WAIT_TIMES = 100;
    private BrokerConfig brokerConfig;

    private QueueTask queueTask;

    private TimerQueue timerQueue;
    private TimerState timerState;

    private MQStore mqStore;
    private TimerStore timerStore;

    private TimerMessageProducer[] timerMessageProducers;
    private TimerTaskScheduler[] timerTaskSchedulers;

    public void initQueueTask(QueueTask task) {
        if (null != queueTask) {
            return;
        }

        this.queueTask = task;
    }

    public QueueTask getOrWaitQueueTask() throws TimeoutException, InterruptedException {
        int i = 0;
        while (i < MAX_WAIT_TIMES) {
            i++;

            if (null != queueTask) {
                return queueTask;
            }

            Thread.sleep(100);
        }

        throw new TimeoutException("wait queue task timeout");
    }

    public void awaitLatch(CountDownLatch latch) throws InterruptedException {
        if (latch.await(1, TimeUnit.SECONDS)) {
            return;
        }

        int successCount = 0;
        while (true) {
            if (isScheduleDone()) {
                successCount++;
                if (successCount >= 2) {
                    break;
                }
            }

            if (latch.await(1, TimeUnit.SECONDS)) {
                break;
            }
        }

        if (!latch.await(1, TimeUnit.SECONDS)) {
            log.warn("CountDownLatch await timeout");
        }
    }

    public boolean isScheduleDone() {
        if (timerQueue.isScheduleQueueEmpty()) {
            return true;
        }

        if (isAllWaiting(timerMessageProducers)) {
            return true;
        }

        return isAllWaiting(timerTaskSchedulers);
    }

    private boolean isAllWaiting(TimerMessageProducer[] timerMessageProducers) {
        for (TimerMessageProducer producer : timerMessageProducers) {
            if (!producer.isWaiting()) {
                return false;
            }
        }

        return true;
    }

    private boolean isAllWaiting(TimerTaskScheduler[] timerTaskSchedulers) {
        for (TimerTaskScheduler scheduler : timerTaskSchedulers) {
            if (scheduler.isWaiting()) {
                return false;
            }
        }

        return true;
    }

}
