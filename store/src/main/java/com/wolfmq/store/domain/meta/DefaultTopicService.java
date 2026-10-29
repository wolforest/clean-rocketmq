package com.wolfmq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.domain.meta.topic.Topic;
import com.wolfmq.domain.domain.meta.topic.TopicMap;
import com.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import com.wolfmq.domain.domain.meta.service.ConsumeOffsetService;
import com.wolfmq.domain.domain.meta.service.TopicService;
import com.wolfmq.domain.domain.meta.topic.KeyBuilder;
import com.wolfmq.store.server.bootstrap.StoreRegister;
import com.wolfmq.domain.server.store.StoreState;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

public class DefaultTopicService implements TopicService {
    private final StoreState storeState;
    private final StoreConfig storeConfig;
    private final String storePath;
    private final ConsumeOffsetService consumeOffsetService;

    // dependency from other package, injected by module manager
    private ConsumeQueueManager consumeQueueManager;
    private StoreRegister storeRegister;

    @Getter
    private TopicMap topicMap;

    public DefaultTopicService(
        StoreState storeState,
        StoreConfig storeConfig,
        String storePath,
        ConsumeOffsetService consumeOffsetService
    ) {
        this.storeState = storeState;
        this.storePath = storePath;
        this.storeConfig = storeConfig;

        this.consumeOffsetService = consumeOffsetService;
        this.topicMap = new TopicMap();
    }

    public void inject(ConsumeQueueManager consumeQueueManager, StoreRegister storeRegister) {
        this.consumeQueueManager = consumeQueueManager;
        this.storeRegister = storeRegister;
    }

    @Override
    public boolean exists(String topicName) {
        if (null == topicMap) {
            return false;
        }

        return topicMap.exists(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return topicMap.getTopic(topicName);
    }

    @Override
    public void saveTopic(Topic topic) {
        topicMap.saveTopic(topic, storeState.getStateMachineVersion());
        this.store();

        storeRegister.registerTopic(topic);
    }

    @Override
    public void putTopic(Topic topic) {
        topicMap.putTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        deleteRetryTopic(topicName);
        cleanTopicInfo(topicName);
    }

    @Override
    public void updateOrderConfig(Map<String, String> orderMap) {

    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            registerSystemTopic();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decode(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(topicMap);
        FileUtil.stringToFile(data, storePath);
    }

    private void registerSystemTopic() {
        SystemTopicRegister register = new SystemTopicRegister(storeConfig,this);
        register.register();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            registerSystemTopic();
            return;
        }

        this.topicMap = JSONUtil.parse(data, TopicMap.class);
        registerSystemTopic();
    }

    private void deleteRetryTopic(String topicName) {
        Set<String> groupSet = consumeOffsetService.findGroupByTopic(topicName);
        for (String group : groupSet) {
            String retryTopic = KeyBuilder.buildPopRetryTopic(topicName, group);
            if (topicMap.exists(retryTopic)) {
                cleanTopicInfo(retryTopic);
            }
        }
    }

    private void cleanTopicInfo(String topicName) {
        consumeOffsetService.deleteByTopic(topicName);
        consumeQueueManager.deleteByTopic(topicName);

        topicMap.deleteTopic(topicName, storeState.getStateMachineVersion());
        this.store();
    }
}
