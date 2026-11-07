package com.wolfmq.common.domain.offset.checkpoint;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CheckpointConfig;
import com.wolfmq.common.domain.offset.checkpoint.store.CheckpointStore;
import com.wolfmq.common.domain.offset.checkpoint.store.FileCheckpointStore;
import com.wolfmq.common.server.store.StoreScheduler;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

public class CheckpointManager implements Lifecycle {
    private final CheckpointConfig config;
    @Getter
    private final Checkpoint checkpoint;
    private final CheckpointStore checkpointStore;
    private final StoreScheduler scheduler;

    public CheckpointManager(StoreConfig storeConfig, StoreScheduler scheduler) {
        this.scheduler = scheduler;
        this.config = storeConfig.getCheckpoint();

        String checkpointDir = storeConfig.getPath().getCheckpointPath();
        checkpointStore = new FileCheckpointStore(checkpointDir);

        checkpoint = new Checkpoint(checkpointStore);
    }

    @Override
    public void initialize() throws Exception {
        checkpointStore.load();
    }

    @Override
    public void start() throws Exception {
        scheduler.scheduleAtFixedRate(
            checkpointStore::save,
            config.getInitialDelay(),
            config.getFlushInterval(),
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void shutdown() throws Exception {
        checkpointStore.save();
    }
}
