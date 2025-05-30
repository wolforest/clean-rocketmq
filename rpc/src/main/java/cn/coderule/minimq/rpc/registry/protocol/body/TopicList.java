package cn.coderule.minimq.rpc.registry.protocol.body;

import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TopicList extends RpcSerializable {
    private Set<String> topicList = ConcurrentHashMap.newKeySet();
    private String brokerAddr;

    public Set<String> getTopicList() {
        return topicList;
    }

    public void setTopicList(Set<String> topicList) {
        this.topicList = topicList;
    }

    public String getBrokerAddr() {
        return brokerAddr;
    }

    public void setBrokerAddr(String brokerAddr) {
        this.brokerAddr = brokerAddr;
    }
}
