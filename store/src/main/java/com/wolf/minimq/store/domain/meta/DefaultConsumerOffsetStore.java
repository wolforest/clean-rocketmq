package com.wolf.minimq.store.domain.meta;

import com.wolf.minimq.domain.service.store.domain.meta.ConsumerOffsetStore;

public class DefaultConsumerOffsetStore implements ConsumerOffsetStore {
    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return 0L;
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return 0L;
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {

    }

    @Override
    public void load() {

    }

    @Override
    public void store() {

    }
}
