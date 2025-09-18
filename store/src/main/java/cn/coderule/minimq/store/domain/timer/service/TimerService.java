package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.common.convention.ability.Flushable;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.store.domain.timer.Timer;
import cn.coderule.minimq.store.domain.mq.queue.MessageService;
import cn.coderule.minimq.store.domain.timer.rocksdb.RocksdbTimer;
import cn.coderule.minimq.store.domain.timer.wheel.DefaultTimer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerService implements Timer, Flushable {
    private final TimerConfig timerConfig;
    private final CheckpointService checkpointService;
    private final MessageService messageService;

    private final Timer timer;

    public TimerService(StoreConfig storeConfig, CheckpointService checkpointService, MessageService messageService) {
        this.timerConfig = storeConfig.getTimerConfig();

        this.checkpointService = checkpointService;
        this.messageService = messageService;

        timer = initTimer(storeConfig);
    }

    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        checkpointService.update(checkpoint);
    }

    public TimerCheckpoint loadCheckpoint() {
        return checkpointService.getCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return timer.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return timer.scan(delayTime);
    }

    @Override
    public void recover() {
        timer.recover();
    }

    private Timer initTimer(StoreConfig storeConfig) {
        try {
            if (!timerConfig.isEnableTimer()) {
                return new BlackHoleTimer();
            }

            if (timerConfig.isEnableRocksDB()) {
                return new RocksdbTimer(storeConfig);
            }

            return new DefaultTimer(storeConfig, checkpointService, messageService);
        } catch (Exception e) {
            log.error("init timer error", e);
        }

        return new BlackHoleTimer();
    }

    @Override
    public void initialize() throws Exception {
        timer.initialize();
    }

    @Override
    public void start() throws Exception {
        timer.start();
    }

    @Override
    public void shutdown() throws Exception {
        timer.shutdown();
    }

    @Override
    public void flush() throws Exception {
        if (timer instanceof Flushable) {
            ((Flushable) timer).flush();
        }

        checkpointService.flush();
    }
}
