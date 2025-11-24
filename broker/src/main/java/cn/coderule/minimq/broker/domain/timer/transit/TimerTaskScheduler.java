package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.MessageResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.store.utils.TimerUtils;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskScheduler extends ServiceThread {
    private final TimerContext timerContext;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;

    private final MQStore mqStore;
    private QueueTask queueTask;

    public TimerTaskScheduler(TimerContext context) {
        this.timerContext = context;
        BrokerConfig brokerConfig = context.getBrokerConfig();
        this.timerConfig = brokerConfig.getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.mqStore = context.getMqStore();
    }

    @Override
    public String getServiceName() {
        return TimerTaskScheduler.class.getSimpleName();
    }

    @Override
    public void run() {
        setState(State.STARTING);
        log.info("{} service started", this.getServiceName());
        if (!loadQueueTask()) {
            return;
        }

        while (!this.isStopped()) {
            try {
                schedule();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
        setState(State.ENDING);
    }

    private void schedule() throws InterruptedException {
        setState(State.WAITING);

        long timeout = 100L * timerConfig.getPrecision() / 1_000;
        List<TimerEvent> eventList = timerQueue.pollScheduleEvent(timeout);;
        if (CollectionUtil.isEmpty(eventList)) {
            return;
        }

        setState(State.RUNNING);
        process(eventList);
        eventList.clear();
    }

    private void process(List<TimerEvent> eventList) {
        int size = eventList.size();
        for (int i = 0; i < size; i++) {
            i = process(eventList.get(i), i);
        }
    }

    private int process(TimerEvent event, int i) {
        boolean success = false;

        try {
            MessageBO message = getMessage(event);
            if (message == null) {
                success = handleNoMessage(event);
                return i;
            }

            int magic = event.getMagic();
            if (TimerUtils.needDelete(magic) && !TimerUtils.needRoll(magic)) {
                success = deleteTimerEvent(event, message);
            } else {
                success = processTimerEvent(event, message, success);
            }

        } catch (Throwable t) {
            success = handleException(t, event, success);
        } finally {
            if (success) i++;
        }

        return i;
    }

    private boolean deleteTimerEvent(TimerEvent event, MessageBO message) {
        String key = message.getProperty(MessageConst.PROPERTY_TIMER_DEL_UNIQKEY);
        if (null != key && null != event.getDeleteList()) {
            event.getDeleteList().add(key);
        }

        event.idempotentRelease();
        return true;
    }

    private boolean enqueueTimerEvent(TimerEvent event, MessageBO message, boolean success) throws InterruptedException {
        event.setMessageBO(message);
        while (!isStopped() && !success) {
            success = timerQueue.offerProduceEvent(event, 3_000);
        }

        return success;
    }

    private boolean processTimerEvent(TimerEvent event, MessageBO message, boolean success) throws InterruptedException {
        String messageId = message.getUniqueKey();
        if (messageId == null) {
            log.warn("enqueueTimerEvent messageId is null: {}",  message);
        }

        if (isInDeleteList(event, messageId)) {
            event.idempotentRelease();
            return true;
        }

        return enqueueTimerEvent(event, message, success);
    }

    private boolean isInDeleteList(TimerEvent event, String messageId) {
        if (null == messageId) {
            return false;
        }
        Set<String> deleteList = event.getDeleteList();
        if (CollectionUtil.isEmpty(deleteList)) {
            return false;
        }

        return deleteList.contains(messageId);
    }

    private boolean handleNoMessage(TimerEvent timerEvent) {
        //the timerRequest will never be processed afterward, so idempotentRelease it
        timerEvent.idempotentRelease();

        return true;
    }

    private MessageBO getMessage(TimerEvent event) {
        MessageRequest request = MessageRequest.builder()
                .storeGroup(queueTask.getStoreGroup())
                .offset(event.getCommitLogOffset())
                .size(event.getMessageSize())
                .build();

        MessageResult result = mqStore.getMessage(request);
        if (result.isSuccess()) {
            return result.getMessage();
        }

        return null;
    }

    private boolean handleException(Throwable e, TimerEvent timerEvent, boolean success) {
        log.error("Unknown exception", e);
        if (timerConfig.isSkipUnknownError()) {
            timerEvent.idempotentRelease();
            success = true;
        } else {
            ThreadUtil.sleep(50);
        }

        return success;
    }

    private boolean loadQueueTask() {
        log.debug("load queue task");

        try {
            queueTask = timerContext.getOrWaitQueueTask();
        } catch (Exception e) {
            log.error("load queue task error", e);
            return false;
        }

        return true;
    }
}
