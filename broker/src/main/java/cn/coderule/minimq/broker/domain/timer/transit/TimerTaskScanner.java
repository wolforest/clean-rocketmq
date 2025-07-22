package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.broker.domain.timer.service.SplitService;
import cn.coderule.minimq.broker.domain.timer.context.TimerContext;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTaskScanner extends ServiceThread {
    private final TimerContext timerContext;
    private final TimerConfig timerConfig;
    private final TimerQueue timerQueue;
    private final TimerState timerState;
    private final TimerStore timerStore;

    private QueueTask queueTask;
    private long startTime;

    public TimerTaskScanner(TimerContext context) {
        this.timerContext = context;
        BrokerConfig brokerConfig = context.getBrokerConfig();
        this.timerConfig = brokerConfig.getTimerConfig();
        this.timerQueue = context.getTimerQueue();
        this.timerState = context.getTimerState();
        this.timerStore = context.getTimerStore();
    }

    @Override
    public String getServiceName() {
        return TimerTaskScanner.class.getSimpleName();
    }

    public void start(long startTime) throws Exception {
        this.startTime = startTime;
        super.start();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());
        if (!loadQueueTask()) {
            return;
        }

        long interval = 100L * timerConfig.getPrecision() / 1_000;
        while (!this.isStopped()) {
            try {
                if (!isTimeToStart()) {
                    continue;
                }

                boolean status = scan();
                if (!status) {
                    await(interval);
                }
            } catch (Throwable t) {
                log.error("{} service has exception. ", this.getServiceName(), t);
            }
        }

        log.info("{} service end", this.getServiceName());
    }

    private boolean isTimeToStart() {
        long now = System.currentTimeMillis();
        if (now < startTime) {
            log.info("{} wait to run at: {}", this.getServiceName(), startTime);
            await(1_000);
            return false;
        }

        return true;
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

    private boolean scan() throws Exception {
        if (!isEnableScan()) {
            return false;
        }

        ScanResult result = timerStore.scan(
            RequestContext.create(queueTask.getStoreGroup()),
            timerState.getLastScanTime()
        );

        if (!shouldParse(result)) {
            return false;
        }

        enqueue(result.getDeleteMsgStack());
        enqueue(result.getNormalMsgStack());

        if (!shouldContinue()) {
            return false;
        }
        timerState.moveScanTime();
        return true;
    }

    private boolean shouldContinue() {
        if (timerState.isHasDequeueException()) {
            return false;
        }

        return timerState.isEnableScan();
    }

    private boolean shouldParse(ScanResult result) {
        if (!result.isSuccess()) {
            return false;
        }

        return timerState.isEnableScan();
    }

    private void enqueue(LinkedList<TimerEvent> msgStack) throws Exception {
        List<List<TimerEvent>> eventGroup = split(msgStack);
        CountDownLatch latch = new CountDownLatch(msgStack.size());

        //read the deleted msg: the msg used to mark another msg is deleted
        for (List<TimerEvent> timerEvents : eventGroup) {
            for (TimerEvent timerEvent : timerEvents) {
                timerEvent.setLatch(latch);
            }
            timerQueue.putScheduleEvent(timerEvents);
        }

        //do we need to use loop with tryAcquire
        timerContext.awaitLatch(latch);
    }

    private boolean isEnableScan() {
        if (timerConfig.isStopConsume()) {
            return false;
        }

        if (!timerState.isEnableScan()) {
            return false;
        }

        return timerState.getLastScanTime() < timerState.getLastSaveTime();
    }

    private List<List<TimerEvent>> split(List<TimerEvent> origin) {
        SplitService splitService = new SplitService(timerConfig.getCommitLogFileSize());
        return splitService.split(origin);
    }
}
