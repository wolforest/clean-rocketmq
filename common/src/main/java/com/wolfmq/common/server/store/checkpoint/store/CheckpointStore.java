package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.CheckpointService;

public interface CheckpointStore extends CheckpointService {
    void load();
    void save();
}
