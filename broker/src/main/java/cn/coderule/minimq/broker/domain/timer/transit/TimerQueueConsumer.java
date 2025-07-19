package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerQueueConsumer extends ServiceThread {
    private final TimerConfig timerConfig;
    private final TimerState timerState;
    private final QueueTask task;

    private final MQStore mqStore;

    public TimerQueueConsumer(TimerContext context, QueueTask task) {
        this.task = task;
        this.timerConfig = context.getBrokerConfig().getTimerConfig();
        this.timerState = context.getTimerState();
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
        if (timerConfig.isStopConsume()) {
            return false;
        }

        DequeueRequest request = createDequeueRequest();
        DequeueResult result = mqStore.get(request);
        if (result.isEmpty()) {
            return false;
        }

        parseResult(result);
        return true;
    }

    private void parseResult(DequeueResult result) {

    }

    private DequeueRequest createDequeueRequest() {
        return DequeueRequest.builder()
            .storeGroup(task.getStoreGroup())
            .group(TimerConstants.TIMER_GROUP)
            .topic(TimerConstants.TIMER_TOPIC)
            .queueId(task.getQueueId())
            .offset(timerState.getTimerQueueOffset())
            .num(timerConfig.getConsumeBatchNum())
            .maxNum(timerConfig.getConsumeMaxNum())
            .build();
    }
}
