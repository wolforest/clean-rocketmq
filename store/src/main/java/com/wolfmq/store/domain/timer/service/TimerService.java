package com.wolfmq.store.domain.timer.service;

import cn.coderule.common.convention.ability.Flushable;
import com.wolfmq.domain.config.business.TimerConfig;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.state.TimerCheckpoint;
import com.wolfmq.domain.domain.timer.model.TimerEvent;
import com.wolfmq.domain.domain.timer.Timer;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.timer.rocksdb.RocksdbTimer;
import com.wolfmq.store.domain.timer.wheel.DefaultTimer;
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
