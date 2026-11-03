package com.wolfmq.common.server.store.checkpoint.store;

import com.wolfmq.common.server.store.checkpoint.model.QueueCheckpoint;
import java.util.TreeMap;

public class TopicCheckpointStore extends CheckpointStore {
    private final MemoryCheckpointStore memoryStore;

    public TopicCheckpointStore(String fileName, MemoryCheckpointStore memoryStore) {
        super(fileName);

        this.memoryStore = memoryStore;
    }

    @Override
    public void load() {

    }

    @Override
    public void save() {
        memoryStore.getQueueTree().forEach(this::saveTopic);
    }

    private void saveTopic(String topic, TreeMap<Integer, QueueCheckpoint> queueMap) {
        queueMap.forEach(this::saveCheckpoint);
    }

    private void saveCheckpoint(int queueId, QueueCheckpoint checkpoint) {
        this.initBuffer()
            .append(checkpoint.getMinOffsetMap())
            .append(checkpoint.getMaxOffsetMap())
            .append(checkpoint.getMinCommitOffsetMap())
            .append(checkpoint.getMaxCommitOffsetMap())
            .save();
    }
}
