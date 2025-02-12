package cn.coderule.minimq.store.domain.meta;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.minimq.domain.model.Topic;
import cn.coderule.minimq.domain.model.meta.TopicTable;
import cn.coderule.minimq.domain.service.store.domain.meta.TopicStore;

public class DefaultTopicStore implements TopicStore {
    private final String storePath;
    private TopicTable topicTable;

    public DefaultTopicStore(String storePath) {
        this.storePath = storePath;
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
    public void putTopic(Topic topic) {

    }

    @Override
    public void deleteTopic(String topicName) {

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
}
