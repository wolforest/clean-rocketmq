package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicTable;
import cn.coderule.minimq.domain.service.store.domain.ConsumeQueueGateway;
import cn.coderule.minimq.domain.service.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicService;
import cn.coderule.minimq.domain.utils.topic.KeyBuilder;
import cn.coderule.minimq.store.server.bootstrap.StoreRegister;
import java.util.Set;

public class DefaultTopicService implements TopicService {
    private final String storePath;
    private final ConsumeOffsetService consumeOffsetService;
    private final ConsumeQueueGateway consumeQueueGateway;
    private final StoreRegister storeRegister;
    private TopicTable topicTable;

    public DefaultTopicService(String storePath,
        ConsumeOffsetService consumeOffsetService,
        ConsumeQueueGateway consumeQueueGateway,
        StoreRegister storeRegister) {
        this.storePath = storePath;
        this.consumeOffsetService = consumeOffsetService;
        this.consumeQueueGateway = consumeQueueGateway;
        this.storeRegister = storeRegister;
    }

    @Override
    public boolean exists(String topicName) {
        if (null == topicTable) {
            return false;
        }

        return topicTable.exists(topicName);
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }

    @Override
    public void saveTopic(Topic topic) {
        topicTable.saveTopic(topic);
        storeRegister.registerTopic(topic);
    }

    @Override
    public void deleteTopic(String topicName) {
        deleteRetryTopic(topicName);
        cleanTopicInfo(topicName);
    }

    @Override
    public void load() {
        if (!FileUtil.exists(storePath)) {
            initTopicTable();
            return;
        }

        String data = FileUtil.fileToString(storePath);
        decodeTopicTable(data);
    }

    @Override
    public void store() {
        String data = JSONUtil.toJSONString(topicTable);
        FileUtil.stringToFile(data, storePath);
    }

    private void initTopicTable() {
        SystemTopicRegister Register = new SystemTopicRegister(this);
        Register.register();
    }

    private void decodeTopicTable(String data) {
        if (StringUtil.isBlank(data)) {
            initTopicTable();
            return;
        }

        this.topicTable = JSONUtil.parse(data, TopicTable.class);
    }

    private void deleteRetryTopic(String topicName) {
        Set<String> groupSet = consumeOffsetService.findGroupByTopic(topicName);
        for (String group : groupSet) {
            String retryTopic = KeyBuilder.buildPopRetryTopic(topicName, group);
            if (topicTable.exists(retryTopic)) {
                cleanTopicInfo(retryTopic);
            }
        }
    }

    private void cleanTopicInfo(String topicName) {
        topicTable.deleteTopic(topicName);
        consumeOffsetService.deleteByTopic(topicName);
        consumeQueueGateway.deleteByTopic(topicName);
    }
}
