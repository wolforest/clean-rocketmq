package cn.coderule.minimq.domain.domain.timer;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TimerQueue {
    public static final int DEFAULT_CAPACITY = 1024;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private final BlockingQueue<TimerEvent> consumeQueue;
    private final BlockingQueue<TimerEvent> produceQueue;
    private final BlockingQueue<List<TimerEvent>> scheduleQueue;


    public TimerQueue(StoreConfig storeConfig) {
        TimerConfig timerConfig = storeConfig.getTimerConfig();

        if (timerConfig.isEnableDisruptor()) {
            this.consumeQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
            this.produceQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
            this.scheduleQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
        } else {
            this.consumeQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
            this.produceQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
            this.scheduleQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
        }
    }

    public boolean isConsumeQueueEmpty() {
        return consumeQueue.isEmpty();
    }

    public boolean isProduceQueueEmpty() {
        return produceQueue.isEmpty();
    }

    public boolean isScheduleQueueEmpty() {
        return scheduleQueue.isEmpty();
    }

    public void putConsumeEvent(TimerEvent event) throws InterruptedException {
        consumeQueue.put(event);
    }

    public boolean offerConsumeEvent(TimerEvent event, long timeout) throws InterruptedException {
        return consumeQueue.offer(event, timeout, DEFAULT_TIME_UNIT);
    }

    public TimerEvent pollConsumeEvent(long timeout) throws InterruptedException {
        return consumeQueue.poll(timeout, DEFAULT_TIME_UNIT);
    }

    public void putProduceEvent(TimerEvent event) throws InterruptedException {
        produceQueue.put(event);
    }

    public boolean offerProduceEvent(TimerEvent event, long timeout) throws InterruptedException {
        return produceQueue.offer(event, timeout, DEFAULT_TIME_UNIT);
    }

    public TimerEvent pollProduceEvent(long timeout) throws InterruptedException {
        return produceQueue.poll(timeout, DEFAULT_TIME_UNIT);
    }

    public void putScheduleEvent(List<TimerEvent> events) throws InterruptedException {
        scheduleQueue.put(events);
    }

    public List<TimerEvent> pollScheduleEvent(long timeout) throws InterruptedException {
        return scheduleQueue.poll(timeout, DEFAULT_TIME_UNIT);
    }
}
