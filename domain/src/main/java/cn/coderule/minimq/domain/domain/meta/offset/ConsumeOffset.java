package cn.coderule.minimq.domain.domain.meta.offset;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.meta.DataVersion;
import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ConsumeOffset implements Serializable {
    public static final String TOPIC_GROUP_SEPARATOR = "@";

    /**
     * topic@group -> queueId -> offset
     */
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable;
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> resetOffsetTable;
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> pullOffsetTable;
    private DataVersion dataVersion;

    public ConsumeOffset() {
        offsetTable = new ConcurrentHashMap<>(512);
        pullOffsetTable = new ConcurrentHashMap<>(512);
        resetOffsetTable = new ConcurrentHashMap<>(512);

        dataVersion = new DataVersion();
    }

    @JSONField(serialize = false)
    public long getOffset(String group, String topic, int queueId) {
        return 0;
    }

    @JSONField(serialize = false)
    public long getAndRemove(String group, String topic, int queueId) {
        return 0;
    }

    @JSONField(serialize = false)
    public Map<Integer, Long> getAll(String group, String topic) {
        return null;
    }

    @JSONField(serialize = false)
    public void putOffset(String group, String topic, int queueId, long offset) {

    }

    @JSONField(serialize = false)
    public void deleteByTopic(String topicName) {
        if (StringUtil.isBlank(topicName)) {
            return;
        }

        Iterator<Entry<String, ConcurrentMap<Integer, Long>>> iterator
            = offsetTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, ConcurrentMap<Integer, Long>> entry = iterator.next();

            String topicAtGroup = entry.getKey();
            if (!topicAtGroup.contains(topicName)) {
                continue;
            }

            String[] arr = topicAtGroup.split(TOPIC_GROUP_SEPARATOR);
            if (arr.length != 2 || !topicName.equals(arr[0])) {
                continue;
            }

            iterator.remove();
            log.warn("delete consumeOffset by topic: key:{}, value:{}",
                topicAtGroup, entry.getValue());
        }
    }

    @JSONField(serialize = false)
    public void deleteByGroup(String groupName) {
        if (StringUtil.isBlank(groupName)) {
            return;
        }

        Iterator<Entry<String, ConcurrentMap<Integer, Long>>> iterator
            = offsetTable.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<String, ConcurrentMap<Integer, Long>> entry = iterator.next();

            String topicAtGroup = entry.getKey();
            if (!topicAtGroup.contains(groupName)) {
                continue;
            }

            String[] arr = topicAtGroup.split(TOPIC_GROUP_SEPARATOR);
            if (arr.length != 2 || !groupName.equals(arr[1])) {
                continue;
            }

            iterator.remove();
            log.warn("delete consumeOffset by group: key:{}, value:{}",
                topicAtGroup, entry.getValue());
        }
    }

    @JSONField(serialize = false)
    public Set<String> findTopicByGroup(String group) {
        if (StringUtil.isBlank(group)) {
            return Set.of();
        }

        Set<String> topicSet = new TreeSet<>();

        for (Entry<String, ConcurrentMap<Integer, Long>> entry : offsetTable.entrySet()) {
            String[] arr = entry.getKey().split(TOPIC_GROUP_SEPARATOR);
            if (arr.length != 2) {
                continue;
            }

            if (group.equals(arr[1])) {
                topicSet.add(arr[0]);
            }
        }

        return topicSet;
    }

    @JSONField(serialize = false)
    public Set<String> findGroupByTopic(String topic) {
        if (StringUtil.isBlank(topic)) {
            return Set.of();
        }

        Set<String> groupSet = new TreeSet<>();
        for (Entry<String, ConcurrentMap<Integer, Long>> entry : offsetTable.entrySet()) {
            String[] arr = entry.getKey().split(TOPIC_GROUP_SEPARATOR);
            if (arr.length != 2) {
                continue;
            }

            if (topic.equals(arr[0])) {
                groupSet.add(arr[1]);
            }
        }

        return groupSet;
    }

}
