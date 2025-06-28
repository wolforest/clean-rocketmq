package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.meta.offset.ConsumeOffset;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import java.util.Set;

public class DefaultConsumeOffsetService implements ConsumeOffsetService {
    private final StoreConfig storeConfig;
    private final String storePath;
    private ConsumeOffset consumeOffset;

    public DefaultConsumeOffsetService(StoreConfig storeConfig, String storePath) {
        this.storeConfig = storeConfig;
        this.storePath = storePath;
    }

    @Override
    public Long getOffset(String group, String topic, int queueId) {
        return consumeOffset.getOffset(group, topic, queueId);
    }

    @Override
    public Long getAndRemove(String group, String topic, int queueId) {
        return consumeOffset.getAndRemove(group, topic, queueId);
    }

    @Override
    public void putOffset(String group, String topic, int queueId, long offset) {
        consumeOffset.putOffset(group, topic, queueId, offset);
    }

    @Override
    public void deleteByTopic(String topicName) {
        consumeOffset.deleteByTopic(topicName);
    }

    @Override
    public void deleteByGroup(String groupName) {
        consumeOffset.deleteByGroup(groupName);
    }

    @Override
    public Set<String> findTopicByGroup(String group) {
        return consumeOffset.findTopicByGroup(group);
    }

    @Override
    public Set<String> findGroupByTopic(String topic) {
        return consumeOffset.findGroupByTopic(topic);
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
        String data = JSONUtil.toJSONString(consumeOffset);
        FileUtil.stringToFile(data, storePath);
    }

    private void init() {
        if (consumeOffset != null) {
            return;
        }

        this.consumeOffset = new ConsumeOffset(storeConfig);
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            init();
            return;
        }

        this.consumeOffset = JSONUtil.parse(data, ConsumeOffset.class);
    }
}
