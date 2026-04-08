
package cn.coderule.wolfmq.rpc.registry.protocol.body;

import cn.coderule.wolfmq.domain.domain.meta.DataVersion;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.codec.RpcSerializable;
import cn.coderule.wolfmq.domain.domain.meta.statictopic.TopicQueueMappingDetail;
import java.util.Map;


public class TopicQueueMappingSerializeWrapper extends RpcSerializable {
    private Map<String/* topic */, TopicQueueMappingDetail> topicQueueMappingInfoMap;
    private DataVersion dataVersion = new DataVersion();

    public Map<String, TopicQueueMappingDetail> getTopicQueueMappingInfoMap() {
        return topicQueueMappingInfoMap;
    }

    public void setTopicQueueMappingInfoMap(Map<String, TopicQueueMappingDetail> topicQueueMappingInfoMap) {
        this.topicQueueMappingInfoMap = topicQueueMappingInfoMap;
    }

    public DataVersion getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(DataVersion dataVersion) {
        this.dataVersion = dataVersion;
    }
}
