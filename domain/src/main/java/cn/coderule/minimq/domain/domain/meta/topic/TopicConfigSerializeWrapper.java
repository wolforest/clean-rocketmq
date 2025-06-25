
package cn.coderule.minimq.domain.domain.model.meta.topic;

import cn.coderule.minimq.domain.domain.model.meta.DataVersion;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TopicConfigSerializeWrapper implements Serializable {
    private ConcurrentMap<String, Topic> topicConfigTable = new ConcurrentHashMap<>();
    private DataVersion dataVersion = new DataVersion();

    public ConcurrentMap<String, Topic> getTopicConfigTable() {
        return topicConfigTable;
    }

    public void setTopicConfigTable(ConcurrentMap<String, Topic> topicConfigTable) {
        this.topicConfigTable = topicConfigTable;
    }

    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }
}
