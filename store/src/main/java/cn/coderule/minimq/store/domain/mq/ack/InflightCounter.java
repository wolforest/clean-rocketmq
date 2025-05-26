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
package cn.coderule.minimq.store.domain.mq.ack;

import cn.coderule.common.lang.type.Pair;
import cn.coderule.minimq.domain.domain.model.consumer.pop.PopCheckPoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InflightCounter {
    private static final String TOPIC_GROUP_SEPARATOR = "@";

    //  topic@group -> queueId -> counter
    private final Map<String, Map<Integer, AtomicLong>> InflightCounter;


    public InflightCounter() {
        this.InflightCounter = new ConcurrentHashMap<>(512);
    }

    public void increment(String topic, String group, int queueId, int num) {
        if (num <= 0) {
            return;
        }
        InflightCounter.compute(buildKey(topic, group), (key, queueNum) -> {
            if (queueNum == null) {
                queueNum = new ConcurrentHashMap<>(8);
            }

            queueNum.compute(queueId, (queueIdKey, counter) -> {
                if (counter == null) {
                    return new AtomicLong(num);
                }
                if (counter.addAndGet(num) <= 0) {
                    return null;
                }
                return counter;
            });
            return queueNum;
        });
    }

    public void decrement(String topic, String group, long popTime, int qId, int delta) {
        decrement(topic, group, qId, delta);
    }

    public void decrement(PopCheckPoint checkPoint) {
        decrement(checkPoint.getTopic(), checkPoint.getCId(), checkPoint.getQueueId(), 1);
    }

    private void decrement(String topic, String group, int queueId, int delta) {
        InflightCounter.computeIfPresent(buildKey(topic, group), (key, queueNum) -> {
            queueNum.computeIfPresent(queueId, (queueIdKey, counter) -> {
                if (counter.addAndGet(-delta) <= 0) {
                    return null;
                }
                return counter;
            });
            if (queueNum.isEmpty()) {
                return null;
            }
            return queueNum;
        });
    }

    public void clearByGroup(String group) {
        Set<String> topicGroupKey = this.InflightCounter.keySet();
        for (String key : topicGroupKey) {
            if (!key.contains(group)) {
                continue;
            }

            Pair<String, String> topicAndGroup = splitKey(key);
            if (topicAndGroup != null && topicAndGroup.getRight().equals(group)) {
                this.InflightCounter.remove(key);
                log.info("PopInflightMessageCounter#clearInFlightMessageNumByGroupName: clean by group, topic={}, group={}",
                    topicAndGroup.getLeft(), topicAndGroup.getRight());
            }
        }
    }

    public void clearByTopic(String topic) {
        Set<String> topicGroupKey = this.InflightCounter.keySet();
        for (String key : topicGroupKey) {
            if (!key.contains(topic)) {
                continue;
            }

            Pair<String, String> topicAndGroup = splitKey(key);
            if (topicAndGroup != null && topicAndGroup.getLeft().equals(topic)) {
                this.InflightCounter.remove(key);
                log.info("PopInflightMessageCounter#clearInFlightMessageNumByTopicName: clean by topic, topic={}, group={}",
                    topicAndGroup.getLeft(), topicAndGroup.getRight());
            }
        }
    }

    public void clear(String topic, String group, int queueId) {
        InflightCounter.computeIfPresent(buildKey(topic, group), (key, queueNum) -> {
            queueNum.computeIfPresent(queueId, (queueIdKey, counter) -> null);
            if (queueNum.isEmpty()) {
                return null;
            }
            return queueNum;
        });
    }

    public long get(String topic, String group, int queueId) {
        Map<Integer /* queueId */, AtomicLong> queueCounter = InflightCounter.get(buildKey(topic, group));
        if (queueCounter == null) {
            return 0;
        }
        AtomicLong counter = queueCounter.get(queueId);
        if (counter == null) {
            return 0;
        }
        return Math.max(0, counter.get());
    }

    private static Pair<String /* topic */, String /* group */> splitKey(String key) {
        String[] strings = key.split(TOPIC_GROUP_SEPARATOR);
        if (strings.length != 2) {
            return null;
        }
        return new Pair<>(strings[0], strings[1]);
    }

    private static String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }
}
