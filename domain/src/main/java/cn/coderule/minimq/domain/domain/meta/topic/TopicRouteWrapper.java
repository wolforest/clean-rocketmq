package cn.coderule.minimq.domain.domain.meta.topic;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.cluster.server.GroupInfo;
import cn.coderule.minimq.domain.domain.cluster.route.QueueInfo;
import cn.coderule.minimq.domain.domain.cluster.route.RouteInfo;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class TopicRouteWrapper {

    private final RouteInfo topicRouteData;
    private final String topicName;
    private final Map<String/* brokerName */, GroupInfo> brokerNameRouteData = new HashMap<>();

    public TopicRouteWrapper(RouteInfo topicRouteData, String topicName) {
        this.topicRouteData = topicRouteData;
        this.topicName = topicName;

        if (this.topicRouteData.getBrokerDatas() != null) {
            for (GroupInfo groupInfo : this.topicRouteData.getBrokerDatas()) {
                this.brokerNameRouteData.put(groupInfo.getBrokerName(), groupInfo);
            }
        }
    }

    public String getMasterAddr(String brokerName) {
        return this.brokerNameRouteData.get(brokerName).getBrokerAddrs().get(MQConstants.MASTER_ID);
    }

    public String getMasterAddrPrefer(String brokerName) {
        Map<Long, String> brokerAddr = brokerNameRouteData.get(brokerName).getBrokerAddrs();
        String addr = brokerAddr.get(MQConstants.MASTER_ID);
        if (addr == null) {
            Optional<Long> optional = brokerAddr.keySet().stream().findFirst();
            return optional.map(brokerAddr::get).orElse(null);
        }
        return addr;
    }

    public String getTopicName() {
        return topicName;
    }

    public RouteInfo getTopicRouteData() {
        return topicRouteData;
    }

    public List<QueueInfo> getQueueDatas() {
        return this.topicRouteData.getQueueDatas();
    }

    public String getOrderTopicConf() {
        return this.topicRouteData.getOrderTopicConf();
    }
}
