package com.wolf.minimq.store.domain.meta;

import com.wolf.common.util.io.FileUtil;
import com.wolf.common.util.lang.JSONUtil;
import com.wolf.common.util.lang.StringUtil;
import com.wolf.minimq.domain.model.meta.ConsumerOffset;
import com.wolf.minimq.domain.service.store.domain.meta.ConsumerOffsetStore;

public class DefaultConsumerOffsetStore implements ConsumerOffsetStore {
    private final String storePath;
    private ConsumerOffset consumerOffset;

    public DefaultConsumerOffsetStore(String storePath) {
        this.storePath = storePath;
    }

    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return consumerOffset.getOffset(group, topic, queueId);
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return consumerOffset.getAndRemove(group, topic, queueId);
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {
        consumerOffset.putOffset(group, topic, queueId, offset);
    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            init();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(consumerOffset);
        FileUtil.stringToFile(data, storePath);
    }

    private void init() {
        if (consumerOffset != null) {
            return;
        }

        this.consumerOffset = new ConsumerOffset();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            init();
            return;
        }

        this.consumerOffset = JSONUtil.parse(data, ConsumerOffset.class);
    }
}
