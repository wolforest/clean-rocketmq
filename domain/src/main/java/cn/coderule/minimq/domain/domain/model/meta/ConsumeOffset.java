package cn.coderule.minimq.domain.domain.model.meta;

import cn.coderule.common.util.lang.StringUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Data;

@Data
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

    public void putOffset(String group, String topic, int queueId, long offset) {

    }

    public Set<String> findTopicByGroup(String group) {
        if (StringUtil.isBlank(group)) {
            return Set.of();
        }

        Set<String> topicSet = new TreeSet<>();

        for (Map.Entry<String, ConcurrentMap<Integer, Long>> entry : offsetTable.entrySet()) {
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

    public Set<String> findGroupByTopic(String topic) {
        if (StringUtil.isBlank(topic)) {
            return Set.of();
        }

        Set<String> groupSet = new TreeSet<>();
        for (Map.Entry<String, ConcurrentMap<Integer, Long>> entry : offsetTable.entrySet()) {
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
