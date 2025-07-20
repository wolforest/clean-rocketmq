package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.broker.domain.timer.service.TimerConverter;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import lombok.extern.slf4j.Slf4j;

/**
 * pull task from in-memory queue, then put message back to commitLog
 * @rocketmq origin name: TimerDequeuePutMessageService
 */
@Slf4j
public class TimerMessageProducer extends ServiceThread {
    private final BrokerConfig brokerConfig;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;
    private final TimerState timerState;

    public TimerMessageProducer(TimerContext context) {
        this.brokerConfig = context.getBrokerConfig();
        this.timerConfig = context.getBrokerConfig().getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.timerState = context.getTimerState();
    }

    @Override
    public String getServiceName() {
        return TimerMessageProducer.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped() || !timerQueue.isProduceQueueEmpty()) {
            try {
                produce();
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private void produce() throws InterruptedException {
        TimerEvent event = timerQueue.pollProduceEvent(10);
        if (event == null) {
            return;
        }

        boolean dequeueFlag = false;
        try {
            dequeueFlag = handleEvent(event);
        } catch (Throwable t) {
            log.error("{} service has exception. ", this.getServiceName(), t);
        } finally {
            event.idempotentRelease(!dequeueFlag);
        }

    }

    private boolean handleEvent(TimerEvent event) {
        boolean stopping = false;
        boolean dequeueException = false;

        while (!isStopped() && !stopping) {
            if (isStopProduce()) {
                timerState.setDequeueExceptionFlag(true);
                dequeueException = true;
                break;
            }

            try {
                MessageBO messageBO = TimerConverter.toMessage(event);

            } catch (Throwable t) {
                stopping = handleHandleException(t, stopping);
            }
        }

        return dequeueException;
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
