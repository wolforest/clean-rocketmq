package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;

public class WheelScanner {
    private final StoreConfig storeConfig;

    public WheelScanner(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    public ScanResult scan(long delayTime) {
        return null;
    }
}
