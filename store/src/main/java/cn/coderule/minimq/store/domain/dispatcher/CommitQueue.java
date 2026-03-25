package cn.coderule.minimq.store.domain.dispatcher;

import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitEvent;
import com.conversantmedia.util.concurrent.DisruptorBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @renamed from CommitEventQueue to CommitQueue
 */
public class CommitQueue {
    public static final int DEFAULT_CAPACITY = 5_000;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    private final CommitConfig config;
    private final BlockingQueue<CommitEvent> queue;

    public CommitQueue(CommitConfig config) {
        this.config = config;

        if (config.isEnableDisruptor()) {
            this.queue = new DisruptorBlockingQueue<>(DEFAULT_CAPACITY);
        } else {
            this.queue = new LinkedBlockingQueue<>(DEFAULT_CAPACITY);
        }
    }

    public void put(CommitEvent event) throws InterruptedException {
        queue.put(event);
    }

    public boolean offer(CommitEvent event) throws InterruptedException {
        return offer(event, config.getDefaultOfferTimeout());
    }

    public boolean offer(CommitEvent event, long timeout) throws InterruptedException {
        return queue.offer(event, timeout, DEFAULT_TIME_UNIT);
    }

    public CommitEvent poll() throws InterruptedException {
        return poll(config.getDefaultPollTimeout());
    }

    public CommitEvent poll(long timeout) throws InterruptedException {
        return queue.poll(timeout, DEFAULT_TIME_UNIT);
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}
