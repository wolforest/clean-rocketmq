package cn.coderule.minimq.store.domain.timer.rocksdb;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.store.domain.timer.Timer;

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
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }
}
