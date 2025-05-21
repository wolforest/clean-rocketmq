/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.minimq.domain.domain.model.cluster.selector;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.constant.PermName;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.meta.topic.TopicRouteWrapper;
import cn.coderule.minimq.domain.service.common.QueueFilter;
import cn.coderule.minimq.domain.domain.model.cluster.route.QueueInfo;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class MessageQueueSelector {
    private static final int BROKER_ACTING_QUEUE_ID = -1;

    // multiple queues for brokers with queueId : normal
    private final List<MessageQueue> queues = new ArrayList<>();
    // one queue for brokers with queueId : -1
    private final List<MessageQueue> brokerActingQueues = new ArrayList<>();
    private final Map<String, MessageQueue> brokerNameQueueMap = new ConcurrentHashMap<>();
    private final AtomicInteger queueIndex;
    private final AtomicInteger brokerIndex;
    private MQFaultStrategy mqFaultStrategy;

    public MessageQueueSelector(TopicRouteWrapper topicRouteWrapper, MQFaultStrategy mqFaultStrategy, boolean read) {
        if (read) {
            this.queues.addAll(buildRead(topicRouteWrapper));
        } else {
            this.queues.addAll(buildWrite(topicRouteWrapper));
        }
        buildBrokerActingQueues(topicRouteWrapper.getTopicName(), this.queues);
        Random random = new Random();
        this.queueIndex = new AtomicInteger(random.nextInt());
        this.brokerIndex = new AtomicInteger(random.nextInt());
        this.mqFaultStrategy = mqFaultStrategy;
    }

    private static List<MessageQueue> buildRead(TopicRouteWrapper topicRoute) {
        Set<MessageQueue> queueSet = new HashSet<>();
        List<QueueInfo> qds = topicRoute.getQueueDatas();
        if (qds == null) {
            return new ArrayList<>();
        }

        for (QueueInfo qd : qds) {
            if (PermName.isReadable(qd.getPerm())) {
                String brokerAddr = topicRoute.getMasterAddrPrefer(qd.getBrokerName());
                if (brokerAddr == null) {
                    continue;
                }

                for (int i = 0; i < qd.getReadQueueNums(); i++) {
                    MessageQueue mq = new MessageQueue(topicRoute.getTopicName(), qd.getBrokerName(), i);
                    mq.setAddress(brokerAddr);
                    queueSet.add(mq);
                }
            }
        }

        return queueSet.stream().sorted().collect(Collectors.toList());
    }

    private static List<MessageQueue> buildWrite(TopicRouteWrapper topicRoute) {
        Set<MessageQueue> queueSet = new HashSet<>();
        // order topic route.
        if (StringUtil.notBlank(topicRoute.getOrderTopicConf())) {
            String[] brokers = topicRoute.getOrderTopicConf().split(";");
            for (String broker : brokers) {
                String[] item = broker.split(":");
                String brokerName = item[0];
                String brokerAddr = topicRoute.getMasterAddr(brokerName);
                if (brokerAddr == null) {
                    continue;
                }

                int nums = Integer.parseInt(item[1]);
                for (int i = 0; i < nums; i++) {
                    MessageQueue mq = new MessageQueue(topicRoute.getTopicName(), brokerName, i);
                    mq.setAddress(brokerAddr);
                    queueSet.add(mq);
                }
            }
        } else {
            List<QueueInfo> qds = topicRoute.getQueueDatas();
            if (qds == null) {
                return new ArrayList<>();
            }

            for (QueueInfo qd : qds) {
                if (PermName.isWriteable(qd.getPerm())) {
                    String brokerAddr = topicRoute.getMasterAddr(qd.getBrokerName());
                    if (brokerAddr == null) {
                        continue;
                    }

                    for (int i = 0; i < qd.getWriteQueueNums(); i++) {
                        MessageQueue mq = new MessageQueue(topicRoute.getTopicName(), qd.getBrokerName(), i);
                        mq.setAddress(brokerAddr);
                        queueSet.add(mq);
                    }
                }
            }
        }

        return queueSet.stream().sorted().collect(Collectors.toList());
    }

    private void buildBrokerActingQueues(String topic, List<MessageQueue> normalQueues) {
        for (MessageQueue mq : normalQueues) {
            if (!brokerActingQueues.contains(mq)) {
                brokerActingQueues.add(mq);
                brokerNameQueueMap.put(mq.getGroupName(), mq);
            }
        }

        Collections.sort(brokerActingQueues);
    }

    public MessageQueue getQueueByBrokerName(String brokerName) {
        return this.brokerNameQueueMap.get(brokerName);
    }

    public MessageQueue selectOne(boolean onlyBroker) {
        int nextIndex = onlyBroker ? brokerIndex.getAndIncrement() : queueIndex.getAndIncrement();
        return selectOneByIndex(nextIndex, onlyBroker);
    }

    public MessageQueue selectOneByPipeline(boolean onlyBroker) {
        if (mqFaultStrategy != null && mqFaultStrategy.isSendLatencyFaultEnable()) {
            List<MessageQueue> messageQueueList = null;
            MessageQueue messageQueue = null;
            if (onlyBroker) {
                messageQueueList = transferAddressableQueues(brokerActingQueues);
            } else {
                messageQueueList = transferAddressableQueues(queues);
            }
            MessageQueue addressableMessageQueue = null;

            // use both available filter.
            messageQueue = selectOneMessageQueue(messageQueueList, onlyBroker ? brokerIndex : queueIndex,
                    mqFaultStrategy.getAvailableFilter(), mqFaultStrategy.getReachableFilter());
            addressableMessageQueue = transferQueue2Addressable(messageQueue);
            if (addressableMessageQueue != null) {
                return addressableMessageQueue;
            }

            // use available filter.
            messageQueue = selectOneMessageQueue(messageQueueList, onlyBroker ? brokerIndex : queueIndex,
                    mqFaultStrategy.getAvailableFilter());
            addressableMessageQueue = transferQueue2Addressable(messageQueue);
            if (addressableMessageQueue != null) {
                return addressableMessageQueue;
            }

            // no available filter, then use reachable filter.
            messageQueue = selectOneMessageQueue(messageQueueList, onlyBroker ? brokerIndex : queueIndex,
                    mqFaultStrategy.getReachableFilter());
            addressableMessageQueue = transferQueue2Addressable(messageQueue);
            if (addressableMessageQueue != null) {
                return addressableMessageQueue;
            }
        }

        // SendLatency is not enabled, or no queue is selected, then select by index.
        return selectOne(onlyBroker);
    }

    private MessageQueue selectOneMessageQueue(List<MessageQueue> messageQueueList, AtomicInteger sendQueue, QueueFilter...filter) {
        if (messageQueueList == null || messageQueueList.isEmpty()) {
            return null;
        }
        if (filter != null && filter.length != 0) {
            for (int i = 0; i < messageQueueList.size(); i++) {
                int index = Math.abs(sendQueue.incrementAndGet() % messageQueueList.size());
                MessageQueue mq = messageQueueList.get(index);
                boolean filterResult = true;
                for (QueueFilter f: filter) {
                    Preconditions.checkNotNull(f);
                    filterResult &= f.filter(mq);
                }
                if (filterResult) {
                    return mq;
                }
            }
        }
        return null;
    }

    public List<MessageQueue> transferAddressableQueues(List<MessageQueue> addressableMessageQueueList) {
        return addressableMessageQueueList;
    }

    private MessageQueue transferQueue2Addressable(MessageQueue messageQueue) {
        for (MessageQueue amq: queues) {
            if (amq.equals(messageQueue)) {
                return amq;
            }
        }
        return null;
    }

    public MessageQueue selectNextOne(MessageQueue last) {
        boolean onlyBroker = last.getQueueId() < 0;
        MessageQueue newOne = last;
        int count = onlyBroker ? brokerActingQueues.size() : queues.size();

        for (int i = 0; i < count; i++) {
            newOne = selectOne(onlyBroker);
            if (!newOne.getGroupName().equals(last.getGroupName()) || newOne.getQueueId() != last.getQueueId()) {
                break;
            }
        }
        return newOne;
    }

    public MessageQueue selectOneByIndex(int index, boolean onlyBroker) {
        if (onlyBroker) {
            if (brokerActingQueues.isEmpty()) {
                return null;
            }
            return brokerActingQueues.get(IntMath.mod(index, brokerActingQueues.size()));
        }

        if (queues.isEmpty()) {
            return null;
        }
        return queues.get(IntMath.mod(index, queues.size()));
    }

    public List<MessageQueue> getQueues() {
        return queues;
    }

    public List<MessageQueue> getBrokerActingQueues() {
        return brokerActingQueues;
    }

    public MQFaultStrategy getMQFaultStrategy() {
        return mqFaultStrategy;
    }

    public void setMQFaultStrategy(MQFaultStrategy mqFaultStrategy) {
        this.mqFaultStrategy = mqFaultStrategy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MessageQueueSelector queue)) {
            return false;
        }
        return Objects.equals(queues, queue.queues)
            && Objects.equals(brokerActingQueues, queue.brokerActingQueues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(queues, brokerActingQueues);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("queues", queues)
            .add("brokerActingQueues", brokerActingQueues)
            .add("brokerNameQueueMap", brokerNameQueueMap)
            .add("queueIndex", queueIndex)
            .add("brokerIndex", brokerIndex)
            .toString();
    }
}
