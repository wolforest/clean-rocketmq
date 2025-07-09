package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.store.infra.StoreScheduler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SynchronizerManager implements Lifecycle, Synchronizer {
    private final List<Synchronizer> synchronizerList;

    private final StoreScheduler storeScheduler;

    public SynchronizerManager(StoreScheduler storeScheduler) {
        this.storeScheduler = storeScheduler;
        this.synchronizerList = new ArrayList<>();
    }

    @Override
    public void initialize() {
        synchronizerList.add(new TimerSynchronizer());
    }

    @Override
    public void start() {
        storeScheduler.scheduleAtFixedRate(
            this::sync,
            10_000,
            3_000,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void sync() {
        for (Synchronizer synchronizer : synchronizerList) {
            synchronizer.sync();
        }
    }
}
