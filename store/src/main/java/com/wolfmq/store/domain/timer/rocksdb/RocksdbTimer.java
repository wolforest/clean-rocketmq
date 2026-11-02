package com.wolfmq.store.domain.timer.rocksdb;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;
import com.wolfmq.common.domain.timer.Timer;

public class RocksdbTimer implements Timer {
    private final StoreConfig storeConfig;

    public RocksdbTimer(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return false;
    }

    @Override
    public ScanResult scan(long delayTime) {
        return null;
    }

    @Override
    public void recover() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
