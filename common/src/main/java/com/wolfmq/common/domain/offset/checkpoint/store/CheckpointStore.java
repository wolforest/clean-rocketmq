package com.wolfmq.common.domain.offset.checkpoint.store;

import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;

public interface CheckpointStore extends CheckpointService {
    void load();
    void save();
}
