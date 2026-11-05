package com.wolfmq.common.server.store.checkpoint.bakeup;

import com.wolfmq.common.server.store.checkpoint.store.FileCheckpointStore;

public class TopicCheckpointStore extends CheckpointStore {
    private final FileCheckpointStore memoryStore;

    public TopicCheckpointStore(String fileName, FileCheckpointStore memoryStore) {
        super(fileName);

        this.memoryStore = memoryStore;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {

    }
}
