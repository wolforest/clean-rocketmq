package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.domain.model.Topic;
import cn.coderule.minimq.domain.domain.model.meta.TopicMap;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.utils.topic.KeyBuilder;
import cn.coderule.minimq.store.infra.StoreRegister;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.util.Map;
import java.util.Set;
import lombok.Getter;

public class DefaultTopicService implements TopicService {
    private final String storePath;
    private final ConsumeOffsetService consumeOffsetService;

    // dependency from other package, injected by module manager
    private ConsumeQueueGateway consumeQueueGateway;
    private StoreRegister storeRegister;

    @Getter
    private TopicMap topicMap;

    public DefaultTopicService(String storePath,
        ConsumeOffsetService consumeOffsetService) {
        this.storePath = storePath;
        this.consumeOffsetService = consumeOffsetService;

        this.topicMap = new TopicMap();
    }

    public void inject(ConsumeQueueGateway consumeQueueGateway, StoreRegister storeRegister) {
        this.consumeQueueGateway = consumeQueueGateway;
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
        topicMap.saveTopic(topic, StoreContext.getStateMachineVersion());
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
        SystemTopicRegister register = new SystemTopicRegister(this);
        register.register();
    }

    private void decode(String data) {
        if (StringUtil.isBlank(data)) {
            registerSystemTopic();
            return;
        }

        this.topicMap = JSONUtil.parse(data, TopicMap.class);
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
        consumeQueueGateway.deleteByTopic(topicName);

        topicMap.deleteTopic(topicName, StoreContext.getStateMachineVersion());
        this.store();
    }
}
