package cn.coderule.minimq.broker.domain.meta;

import cn.coderule.common.lang.concurrent.thread.ThreadLocalSequence;
import cn.coderule.minimq.domain.domain.constant.MQConstants;
import cn.coderule.minimq.domain.domain.constant.PermName;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.rpc.registry.protocol.cluster.GroupInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.QueueInfo;
import cn.coderule.minimq.rpc.registry.protocol.route.RouteInfo;
import cn.coderule.minimq.rpc.registry.protocol.statictopic.TopicQueueMappingInfo;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
public class PublishInfo implements Serializable {
    private boolean ordered = false;
    private boolean hasRoute = false;

    private RouteInfo routeInfo;
    private List<MessageQueue> queueList;

    private volatile ThreadLocalSequence sequence;

    public PublishInfo() {
        this.queueList = new ArrayList<>();
        this.sequence = new ThreadLocalSequence();
    }

    public void resetSequence() {
        this.sequence.reset();
    }

    public static PublishInfo of(final String topic, final RouteInfo route) {
        PublishInfo info = new PublishInfo();
        // TO DO should check the usage of raw route, it is better to remove such field
        info.setRouteInfo(route);
        if (route.getOrderTopicConf() != null && !route.getOrderTopicConf().isEmpty()) {
            String[] brokers = route.getOrderTopicConf().split(";");
            for (String broker : brokers) {
                String[] item = broker.split(":");
                int nums = Integer.parseInt(item[1]);
                for (int i = 0; i < nums; i++) {
                    MessageQueue mq = new MessageQueue(topic, item[0], i);
                    info.getQueueList().add(mq);
                }
            }

            info.setOrdered(true);
        } else if (route.getOrderTopicConf() == null
            && route.getTopicQueueMappingByBroker() != null
            && !route.getTopicQueueMappingByBroker().isEmpty()) {
            info.setOrdered(false);
            ConcurrentMap<MessageQueue, String> mqEndPoints = getQueueMap(topic, route);
            info.getQueueList().addAll(mqEndPoints.keySet());
            info.getQueueList().sort(Comparator.comparingInt(MessageQueue::getQueueId));
        } else {
            List<QueueInfo> qds = route.getQueueDatas();
            Collections.sort(qds);
            for (QueueInfo qd : qds) {
                if (PermName.isWriteable(qd.getPerm())) {
                    GroupInfo groupInfo = null;
                    for (GroupInfo bd : route.getBrokerDatas()) {
                        if (bd.getBrokerName().equals(qd.getBrokerName())) {
                            groupInfo = bd;
                            break;
                        }
                    }

                    if (null == groupInfo) {
                        continue;
                    }

                    if (!groupInfo.getBrokerAddrs().containsKey(MQConstants.MASTER_ID)) {
                        continue;
                    }

                    for (int i = 0; i < qd.getWriteQueueNums(); i++) {
                        MessageQueue mq = new MessageQueue(topic, qd.getBrokerName(), i);
                        info.getQueueList().add(mq);
                    }
                }
            }

            info.setOrdered(false);
        }

        return info;
    }

    public static Set<MessageQueue> getQueueSet(final String topic, final RouteInfo route) {
        if (!route.isQueueMappingEmpty()) {
            return getQueueMap(topic, route).keySet();
        }

        Set<MessageQueue> queueSet = new HashSet<>();
        for (QueueInfo qd : route.getQueueDatas()) {
            if (!PermName.isReadable(qd.getPerm())) {
                continue;
            }

            for (int i = 0; i < qd.getReadQueueNums(); i++) {
                MessageQueue mq = new MessageQueue(topic, qd.getBrokerName(), i);
                queueSet.add(mq);
            }
        }

        return queueSet;
    }

    private static Map<String, Map<String, TopicQueueMappingInfo>> getScopeMap(RouteInfo route) {
        Map<String, Map<String, TopicQueueMappingInfo>> scopeMap = new HashMap<>();
        for (Map.Entry<String, TopicQueueMappingInfo> entry : route.getTopicQueueMappingByBroker().entrySet()) {
            TopicQueueMappingInfo info = entry.getValue();
            String scope = info.getScope();
            if (scope != null) {
                if (!scopeMap.containsKey(scope)) {
                    scopeMap.put(scope, new HashMap<>());
                }
                scopeMap.get(scope).put(entry.getKey(), entry.getValue());
            }
        }

        return scopeMap;
    }

    private static List<Map.Entry<String, TopicQueueMappingInfo>> getMappingList(Map<String, TopicQueueMappingInfo> topicMap) {
        List<Map.Entry<String, TopicQueueMappingInfo>> mappingList = new ArrayList<>(topicMap.entrySet());
        mappingList.sort((o1, o2) -> (int) (o2.getValue().getEpoch() - o1.getValue().getEpoch()));

        return mappingList;
    }

    public static ConcurrentMap<MessageQueue, String> getQueueMap(final String topic, final RouteInfo route) {
        if (route.isQueueMappingEmpty()) {
            return new ConcurrentHashMap<>();
        }

        ConcurrentMap<MessageQueue, String> queueMap = new ConcurrentHashMap<>();
        Map<String, Map<String, TopicQueueMappingInfo>> scopeMap = getScopeMap(route);

        for (Map.Entry<String, Map<String, TopicQueueMappingInfo>> mapEntry : scopeMap.entrySet()) {
            int totalQueues = 0;
            ConcurrentMap<MessageQueue, TopicQueueMappingInfo> mqMap = new ConcurrentHashMap<>();

            for (Map.Entry<String, TopicQueueMappingInfo> entry : getMappingList(mapEntry.getValue())) {
                TopicQueueMappingInfo info = entry.getValue();
                if (info.getEpoch() >= -1 && info.getTotalQueues() > totalQueues) {
                    totalQueues = info.getTotalQueues();
                }

                putMqMap(mqMap, topic, entry);
            }

            putQueueMap(queueMap, topic, mapEntry.getKey(), mqMap, totalQueues);
        }
        return queueMap;
    }

    private static void putMqMap(ConcurrentMap<MessageQueue, TopicQueueMappingInfo> mqMap, String topic, Map.Entry<String, TopicQueueMappingInfo> entry) {
        TopicQueueMappingInfo info = entry.getValue();

        for (Map.Entry<Integer, Integer> idEntry : entry.getValue().getCurrIdMap().entrySet()) {
            int globalId = idEntry.getKey();
            MessageQueue mq = new MessageQueue(topic, getMockBrokerName(entry.getValue().getScope()), globalId);
            TopicQueueMappingInfo oldInfo = mqMap.get(mq);

            if (oldInfo == null ||  oldInfo.getEpoch() <= info.getEpoch()) {
                mqMap.put(mq, info);
            }
        }
    }

    private static void putQueueMap(ConcurrentMap<MessageQueue, String> queueMap, String topic, String scope, Map<MessageQueue, TopicQueueMappingInfo> mqEndPoints,int maxTotalNums) {
        //accomplish the static logic queues
        for (int i = 0; i < maxTotalNums; i++) {
            MessageQueue mq = new MessageQueue(topic, getMockBrokerName(scope), i);
            if (!mqEndPoints.containsKey(mq)) {
                queueMap.put(mq, MQConstants.LOGICAL_QUEUE_MOCK_BROKER_NAME_NOT_EXIST);
            } else {
                queueMap.put(mq, mqEndPoints.get(mq).getBname());
            }
        }
    }

    public static String getMockBrokerName(String scope) {
        assert scope != null;
        if (scope.equals(MQConstants.METADATA_SCOPE_GLOBAL)) {
            return MQConstants.LOGICAL_QUEUE_MOCK_BROKER_PREFIX + scope.substring(2);
        } else {
            return MQConstants.LOGICAL_QUEUE_MOCK_BROKER_PREFIX + scope;
        }
    }



}
