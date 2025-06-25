
package cn.coderule.minimq.rpc.registry.protocol.body;

import cn.coderule.minimq.domain.domain.meta.DataVersion;
import cn.coderule.minimq.domain.domain.meta.topic.TopicConfigSerializeWrapper;
import cn.coderule.minimq.domain.domain.meta.statictopic.TopicQueueMappingDetail;
import cn.coderule.minimq.domain.domain.meta.statictopic.TopicQueueMappingInfo;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TopicConfigAndMappingSerializeWrapper extends TopicConfigSerializeWrapper {
    private Map<String/* topic */, TopicQueueMappingInfo> topicQueueMappingInfoMap = new ConcurrentHashMap<>();

    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingDetailMap = new ConcurrentHashMap<>();

    private DataVersion mappingDataVersion = new DataVersion();


    public Map<String, TopicQueueMappingInfo> getTopicQueueMappingInfoMap() {
        return topicQueueMappingInfoMap;
    }

    public void setTopicQueueMappingInfoMap(Map<String, TopicQueueMappingInfo> topicQueueMappingInfoMap) {
        this.topicQueueMappingInfoMap = topicQueueMappingInfoMap;
    }

    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingDetailMap() {
        return topicQueueMappingDetailMap;
    }

    public void setTopicQueueMappingDetailMap(Map<String, TopicQueueMappingDetail> topicQueueMappingDetailMap) {
        this.topicQueueMappingDetailMap = topicQueueMappingDetailMap;
    }

    public DataVersion getMappingDataVersion() {
        return mappingDataVersion;
    }

    public void setMappingDataVersion(DataVersion mappingDataVersion) {
        this.mappingDataVersion = mappingDataVersion;
    }

    public static TopicConfigAndMappingSerializeWrapper from(TopicConfigSerializeWrapper wrapper) {
        if (wrapper instanceof  TopicConfigAndMappingSerializeWrapper) {
            return (TopicConfigAndMappingSerializeWrapper) wrapper;
        }
        TopicConfigAndMappingSerializeWrapper mappingSerializeWrapper =  new TopicConfigAndMappingSerializeWrapper();
        mappingSerializeWrapper.setDataVersion(wrapper.getDataVersion());
        mappingSerializeWrapper.setTopicConfigTable(wrapper.getTopicConfigTable());
        return mappingSerializeWrapper;
    }
}
