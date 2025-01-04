package com.wolf.minimq.store.domain.meta;

import com.wolf.common.util.io.FileUtil;
import com.wolf.common.util.lang.JSONUtil;
import com.wolf.common.util.lang.StringUtil;
import com.wolf.minimq.domain.model.Topic;
import com.wolf.minimq.domain.model.meta.TopicTable;
import com.wolf.minimq.domain.service.store.domain.meta.TopicStore;

public class DefaultTopicStore implements TopicStore {
    private final String storePath;
    private TopicTable topicTable;

    public DefaultTopicStore(String storePath) {
        this.storePath = storePath;
    }

    @Override
    public Topic getTopic(String topicName) {
        return null;
    }

    @Override
    public void putTopic(Topic topic) {

    }

    @Override
    public void deleteTopic(String topicName) {

    }

    @Override
    public void load() {
        if (!FileUtil.isFileExists(storePath)) {
            initTopicTable();
            return;
        }

        String data = FileUtil.readString(storePath);
        decodeTopicTable(data);
    }

    @Override
    public void persist() {
        String data = JSONUtil.toJSONString(topicTable);
        FileUtil.writeString(storePath, data);
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
}
