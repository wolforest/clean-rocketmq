package cn.coderule.minimq.domain.domain.meta.offset;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.config.store.MetaConfig;
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
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ConsumeOffset implements Serializable {
    public static final String TOPIC_GROUP_SEPARATOR = "@";
    private StoreConfig storeConfig;
    private MetaConfig metaConfig;

    /**
     * topic@group -> queueId -> offset
     */
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> offsetTable;
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> resetOffsetTable;
    private ConcurrentMap<String, ConcurrentMap<Integer, Long>> pullOffsetTable;

    private DataVersion dataVersion;
    private final transient AtomicLong versionCounter;


    public ConsumeOffset() {
        offsetTable = new ConcurrentHashMap<>(512);
        pullOffsetTable = new ConcurrentHashMap<>(512);
        resetOffsetTable = new ConcurrentHashMap<>(512);

        dataVersion = new DataVersion();
        versionCounter = new AtomicLong(0);
    }

    public ConsumeOffset(StoreConfig storeConfig) {
        this();

        setStoreConfig(storeConfig);
    }

    public void setStoreConfig(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
        this.metaConfig = storeConfig.getMetaConfig();
    }

    @JSONField(serialize = false)
    public long getOffset(String group, String topic, int queueId) {
        String key = buildKey(topic, group);
        ConcurrentMap<Integer, Long> map = offsetTable.get(key);
        if (map == null) {
            return 0;
        }

        return map.get(queueId);
    }

    @JSONField(serialize = false)
    public long getAndRemove(String group, String topic, int queueId) {
        String key = buildKey(topic, group);
        ConcurrentMap<Integer, Long> map = offsetTable.get(key);
        if (map == null) {
            return 0;
        }

        Long old = map.remove(queueId);

        return old == null ? 0L : old;
    }

    @JSONField(serialize = false)
    public Map<Integer, Long> getAll(String group, String topic) {
        String key = buildKey(topic, group);
        return offsetTable.get(key);
    }

    @JSONField(serialize = false)
    public void putOffset(String group, String topic, int queueId, long offset) {
        String key = buildKey(topic, group);
        offsetTable.computeIfAbsent(key, k -> new ConcurrentHashMap<>(32));

        ConcurrentMap<Integer, Long> map = offsetTable.get(key);

        Long oldOffset = map.put(queueId, offset);
        if (oldOffset != null && offset < oldOffset) {
            log.warn("[NOTIFYME] consume offset error, new offset is less than old offset,"
                + "key={}, queueId={}, newOffset={}, oldOffset={}",
                key, queueId, offset, oldOffset
            );
        }

        long step = versionCounter.incrementAndGet();
        if (step % metaConfig.getConsumeOffsetVersionUpdateStep() == 0) {
           dataVersion.nextVersion(0);
        }
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

    private String buildKey(String topic, String group) {
        return topic + TOPIC_GROUP_SEPARATOR + group;
    }

}
