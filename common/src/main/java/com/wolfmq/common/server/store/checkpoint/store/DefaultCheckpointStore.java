package com.wolfmq.common.server.store.checkpoint.store;

public class DefaultCheckpointStore extends CheckpointStore {
    private final MemoryCheckpointStore memoryStore;

    public DefaultCheckpointStore(String fileName, MemoryCheckpointStore memoryStore) {
        super(fileName);

        this.memoryStore = memoryStore;
    }


    @Override
    public void load() {

    }

    @Override
    public void save() {
        this.initBuffer()
            .append(memoryStore.getCommit().getMaxOffsetMap())
            .append(memoryStore.getDispatcher().getOffsetMap())
            .append(memoryStore.getIndex().getMaxOffsetMap())
            .append(memoryStore.getMaster().getOffsetMap())
            .persist();
    }

}
