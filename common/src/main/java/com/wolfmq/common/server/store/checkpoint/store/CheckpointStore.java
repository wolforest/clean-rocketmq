package com.wolfmq.common.server.store.checkpoint.store;

public interface CheckpointStore {
    void load();
    void save();
}
