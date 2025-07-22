package cn.coderule.minimq.broker.domain.timer.transit;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.timer.service.TimerContext;
import cn.coderule.minimq.broker.infra.store.TimerStore;
import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.TimerQueue;
import cn.coderule.minimq.domain.domain.timer.state.TimerState;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

    private boolean scan() {
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
        parse(result);

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

    private void parse(ScanResult result) {

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

    private List<TimerEvent> initList(
        List<List<TimerEvent>> lists,
        List<TimerEvent> list,
        TimerEvent event
    ) {
        if (!CollectionUtil.isEmpty(list)) {
            lists.add(list);
        }
        list = new LinkedList<>();
        list.add(event);

        return list;
    }

    private List<List<TimerEvent>> splitIntoLists(List<TimerEvent> origin) {
        //this method assume that the origin is not null;
        List<List<TimerEvent>> lists = new LinkedList<>();
        if (origin.size() < 100) {
            lists.add(origin);
            return lists;
        }

        int msgIndex = 0;
        int fileIndex = -1;
        List<TimerEvent> list = null;

        for (TimerEvent event : origin) {
            int index = (int) (event.getCommitLogOffset() / timerConfig.getCommitLogFileSize());
            if (fileIndex != index) {
                msgIndex = 0;
                fileIndex = index;
                list = initList(lists, list, event);
                continue;
            }

            assert list != null;
            list.add(event);
            if (++msgIndex % 2000 == 0) {
                lists.add(list);
                list = new ArrayList<>();
            }
        }

        if (!CollectionUtil.isEmpty(list)) {
            lists.add(list);
        }

        return lists;
    }
}
