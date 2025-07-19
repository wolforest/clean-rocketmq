package cn.coderule.minimq.domain.domain.timer;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TimerQueue {
    public static final int DEFAULT_CAPACITY = 1024;

    private final TimerConfig timerConfig;

    private final BlockingQueue<TimerEvent> consumeQueue;
    private final BlockingQueue<TimerEvent> produceQueue;
    private final BlockingQueue<List<TimerEvent>> scheduleQueue;


    public TimerQueue(StoreConfig storeConfig) {
        timerConfig = storeConfig.getTimerConfig();

        this.consumeQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
        this.produceQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
        this.scheduleQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);

//        if (timerConfig.isEnableDisruptor()) {
//            this.consumeQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
//            this.produceQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
//            this.scheduleQueue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
//        } else {
//            this.consumeQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
//            this.produceQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
//            this.scheduleQueue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
//        }
    }


}
