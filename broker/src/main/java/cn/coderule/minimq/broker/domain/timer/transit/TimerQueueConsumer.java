package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.domain.timer.service.TimerConverter;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import lombok.extern.slf4j.Slf4j;

/**
 * consume timer queue, then enqueue in-memory queue
 * @rocketmq origin name: TimerEnqueueGetService
 */
@Slf4j
public class TimerQueueConsumer extends ServiceThread {
    private final TimerConfig timerConfig;
    private final TimerState timerState;
    private final TimerQueue timerQueue;
    private final QueueTask task;

    private final MQStore mqStore;

    public TimerQueueConsumer(TimerContext context, QueueTask task) {
        this.task = task;
        this.timerConfig = context.getBrokerConfig().getTimerConfig();
        this.timerState = context.getTimerState();
        this.timerQueue = context.getTimerQueue();
        this.mqStore = context.getMqStore();
    }

    @Override
    public String getServiceName() {
        return TimerQueueConsumer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        long interval = 100L * timerConfig.getPrecision() / 1_000;

        while (!this.isStopped()) {
            try {
                if (!consume()) {
                    this.await(interval);
                }
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private boolean consume() {
        if (isStopConsume()) {
            return false;
        }

        DequeueResult result = pullMessage();
        if (result.isEmpty()) {
            return false;
        }

        return parseMessage(result);
    }

    private boolean parseMessage(DequeueResult result) {
        long offset = timerState.getTimerQueueOffset();
        for (MessageBO messageBO : result.getMessageList()) {
            offset++;
            messageBO.setQueueOffset(offset);

            long now = System.currentTimeMillis();
            timerState.setLatestTimerMessageTime(now);
            timerState.setLatestTimerMessageStoreTime(messageBO.getStoreTimestamp());

            TimerEvent event = TimerConverter.toEvent(messageBO, now, TimerConstants.MAGIC_DEFAULT);
            if (!enqueueEvent(event)) {
                return false;
            }

            timerState.setTimerQueueOffset(offset);
        }

        return true;
    }

    private boolean enqueueEvent(TimerEvent event) {
        while (true) {
            try {
                if (timerQueue.offerConsumeEvent(event, 3_000)) {
                    return true;
                }
            } catch (InterruptedException ignore) {
            }

            if (isStopConsume()) {
                return false;
            }
        }
    }

    private DequeueResult pullMessage() {
        DequeueRequest request = DequeueRequest.builder()
            .storeGroup(task.getStoreGroup())
            .group(TimerConstants.TIMER_GROUP)
            .topic(TimerConstants.TIMER_TOPIC)
            .queueId(task.getQueueId())
            .offset(timerState.getTimerQueueOffset())
            .maxNum(timerConfig.getConsumeMaxNum())
            .build();

        return mqStore.get(request);
    }

    private boolean isStopConsume() {
        if (timerConfig.isStopConsume()) {
            return true;
        }

        return !timerState.isRunning();
    }
}
