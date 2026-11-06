package com.wolfmq.store.domain.commitlog.route;

import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.commitlog.route.CommitLogRouter;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.store.domain.commitlog.file.CommitLogManager;

public class DefaultRouter implements CommitLogRouter {
    private final CommitConfig config;
    private final CommitLogManager manager;

    public DefaultRouter(CommitConfig config, CommitLogManager manager) {
        this.config = config;
        this.manager = manager;
    }

    @Override
    public CommitLog route(MessageBO messageBO) {
        return manager.selectByRandom();
    }

    @Override
    public CommitLog route(long offset) {
        int shardId = offsetToShardId(offset);
        return manager.selectByIndex(shardId);
    }

    @Override
    public CommitLog route(int shardId) {
        return manager.selectByIndex(shardId);
    }

    private int offsetToShardId(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        return (int) (offset % config.getMaxShardingNumber());
    }
}
