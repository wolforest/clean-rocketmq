package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.domain.timer.service.TimerConverter;
import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.EnqueueRequest;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import cn.coderule.minimq.domain.utils.TimerUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * pull task from in-memory queue, then put message back to commitLog
 * @rocketmq origin name: TimerDequeuePutMessageService
 */
@Slf4j
public class TimerMessageProducer extends ServiceThread {
    private final TimerContext timerContext;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;
    private final TimerState timerState;
    private final MQStore mqStore;

    private QueueTask queueTask;

    public TimerMessageProducer(TimerContext context) {
        this.timerContext = context;
        this.timerConfig = context.getBrokerConfig().getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.timerState = context.getTimerState();
        this.mqStore = context.getMqStore();
        this.queueTask = context.getQueueTask();
    }

    @Override
    public String getServiceName() {
        return TimerMessageProducer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        setState(State.STARTING);
        waitQueueTask();

        while (!this.isStopped() || !timerQueue.isProduceQueueEmpty()) {
            try {
                produce();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
        setState(State.ENDING);
    }

    private void waitQueueTask() {
        while (true) {
            if (null != queueTask) {
                return;
            }

            if (null != timerContext.getQueueTask()) {
                this.queueTask = timerContext.getQueueTask();
                return;
            }

            ThreadUtil.sleep(100);
        }
    }

    private void produce() throws InterruptedException {
        setState(State.WAITING);
        TimerEvent event = timerQueue.pollProduceEvent(10);
        if (event == null) {
            return;
        }

        boolean dequeueFlag = false;
        try {
            setState(State.RUNNING);
            dequeueFlag = handleEvent(event);
        } catch (Throwable t) {
            log.error("{} service has exception. ", this.getServiceName(), t);
        } finally {
            event.idempotentRelease(!dequeueFlag);
        }

    }

    private boolean handleEvent(TimerEvent event) {
        boolean success = false;
        boolean dequeueException = false;

        while (!isStopped() && !success) {
            if (isStopProduce()) {
                timerState.setHasDequeueException(true);
                dequeueException = true;
                break;
            }

            try {
                MessageBO messageBO = TimerConverter.toMessage(event);
                int status = storeMessage(messageBO, event);

                success = status != TimerConstants.PUT_NEED_RETRY;
                if (!success) {
                    dequeueException = restoreMessage(messageBO, event, dequeueException);
                }
            } catch (Throwable t) {
                success = handleHandleException(t, success);
            }
        }

        return dequeueException;
    }

    private boolean restoreMessage(MessageBO messageBO, TimerEvent event, boolean dequeueException) throws InterruptedException {
        boolean success = false;
        while (!success && !isStopped()) {
            if (isStopProduce()) {
                timerState.setHasDequeueException(true);
                dequeueException = true;
            }

            int status = storeMessage(messageBO, event);
            success = status != TimerConstants.PUT_NEED_RETRY;
            long waitTime = 500L * timerConfig.getPrecision() / 1000;
            ThreadUtil.sleep(waitTime);
        }
        return dequeueException;
    }

    private int storeMessage(MessageBO messageBO, TimerEvent event) {
        boolean needRoll = TimerUtils.needRoll(event.getMagic());
        if (!needRoll && null != messageBO.getProperty(MessageConst.PROPERTY_TIMER_DEL_UNIQKEY)) {
            log.warn("trying to put deleted timer msg: message={}", messageBO);
            return TimerConstants.PUT_NO_RETRY;
        }

        EnqueueRequest request = createEnqueueRequest(messageBO);
        EnqueueResult result = mqStore.enqueue(request);

        int retryTimes = 0;
        while (retryTimes < 3) {
            if (result.isSuccess()) {
                return TimerConstants.PUT_OK;
            }

            retryTimes++;
            ThreadUtil.sleep(50);

            result = mqStore.enqueue(request);
            log.warn("retrying to enqueue timer message: retryTimes:{}, message={}, result={}",
                retryTimes, messageBO, result);
        }

        return TimerConstants.PUT_NO_RETRY;
    }

    private EnqueueRequest createEnqueueRequest(MessageBO messageBO) {
        return EnqueueRequest.builder()
            .storeGroup(queueTask.getStoreGroup())
            .messageBO(messageBO)
            .build();
    }

    private boolean handleHandleException(Throwable t, boolean stopping) {
        log.info("handle timer event exception", t);
        if (timerConfig.isSkipUnknownError()) {
            return true;
        }

        ThreadUtil.sleep(50);
        return stopping;
    }

    private boolean isStopProduce() {
        return !timerState.isRunning();
    }
}
