package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.cluster.store.domain.timer.Timer;
import cn.coderule.minimq.store.domain.timer.rocksdb.RocksdbTimer;
import cn.coderule.minimq.store.domain.timer.wheel.DefaultTimer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerService implements Timer {
    private final TimerConfig timerConfig;
    private final CheckpointService checkpointService;

    private final Timer timer;

    public TimerService(StoreConfig storeConfig,
        CheckpointService checkpointService) {
        this.timerConfig = storeConfig.getTimerConfig();
        this.checkpointService = checkpointService;

        timer = initTimer(storeConfig);
    }

    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {
        timer.storeCheckpoint(checkpoint);
    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return timer.loadCheckpoint();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return timer.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return timer.scan(delayTime);
    }

    private Timer initTimer(StoreConfig storeConfig) {
        try {
            if (!timerConfig.isEnableTimer()) {
                return new BlackHoleTimer();
            }

            if (timerConfig.isEnableRocksDB()) {
                return new RocksdbTimer(storeConfig, checkpointService);
            }

            return new DefaultTimer(storeConfig, checkpointService);
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
}
