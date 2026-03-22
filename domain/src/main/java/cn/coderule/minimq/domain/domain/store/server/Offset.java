package cn.coderule.minimq.domain.domain.store.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Offset implements Serializable {
    private volatile long commitLogOffset = -1;
    private volatile long dispatchedOffset = -1;
    private volatile long indexOffset = -1;

    /**
     * shardId -> offset
     */
    @Setter
    private ConcurrentMap<Integer, Long> commitOffsetMap
        = new ConcurrentHashMap<>(16);

    /**
     * shardId -> offset
     */
    @Setter
    private ConcurrentMap<Integer, Long> dispatchedOffsetMap
        = new ConcurrentHashMap<>(16);

    /**
     * topic -> [ queueId : offset ]
     */
    @Setter
    private ConcurrentMap<String, ArrayList<Long>> topicOffsetMap
        = new ConcurrentHashMap<>(16);

    public void setCommitLogOffset(long commitLogOffset) {
        if (commitLogOffset < this.commitLogOffset) {
            return;
        }

        this.commitLogOffset = commitLogOffset;
    }

    public void setCommitOffset(int shardId, long commitOffset) {
        Long old = commitOffsetMap.get(shardId);
        if (old == null || old > commitOffset) {
            return;
        }

        commitOffsetMap.put(shardId, commitOffset);

    }

    public void setDispatchedOffset(int shardId, long dispatchedOffset) {
        Long old = dispatchedOffsetMap.get(shardId);
        if (old == null || old > dispatchedOffset) {
            return;
        }

        dispatchedOffsetMap.put(shardId, dispatchedOffset);

    }

    public void setDispatchedOffset(long dispatchedOffset) {
        if (dispatchedOffset < this.dispatchedOffset) {
            return;
        }

        this.dispatchedOffset = dispatchedOffset;
    }

    public void setIndexOffset(long indexOffset) {
        if (indexOffset < this.indexOffset) {
            return;
        }

        this.indexOffset = indexOffset;
    }

    public long getQueueOffset(String topic, Integer queueId) {
        ArrayList<Long> queueOffsets = topicOffsetMap.get(topic);
        if (queueOffsets == null) {
            queueOffsets = new ArrayList<>(16);
            topicOffsetMap.putIfAbsent(topic, queueOffsets);
            return 0;
        }

        if (queueId > queueOffsets.size()) {
            return 0;
        }

        Long offset = queueOffsets.get(queueId);
        return offset == null ? 0 : offset;
    }

    public void setQueueOffset(String topic, Integer queueId, long offset) {
        ArrayList<Long> queueOffsets = topicOffsetMap.get(topic);
        if (queueOffsets != null) {
            queueOffsets.set(queueId, offset);
            return;
        }

        queueOffsets = new ArrayList<>(16);
        ArrayList<Long> old = topicOffsetMap.putIfAbsent(topic, queueOffsets);

        if (old == null) {
            queueOffsets.set(queueId, offset);
        } else {
            old.set(queueId, offset);
        }

    }

    public Offset deepCopy() {
        Offset tmp = new Offset();

        tmp.setCommitLogOffset(commitLogOffset);
        tmp.setDispatchedOffset(dispatchedOffset);
        tmp.setIndexOffset(indexOffset);

        deepCopyTopicOffsetMap(tmp);
        deepCopyCommitOffsetMap(tmp);
        deepCopyDispatchedOffsetMap(tmp);

        return tmp;
    }

    private void deepCopyTopicOffsetMap(Offset tmp) {
        ConcurrentMap<String, ArrayList<Long>> topicMap
            = new ConcurrentHashMap<>(topicOffsetMap.size());

        for (String topic : topicOffsetMap.keySet()) {
            ArrayList<Long> queueOffsets = topicOffsetMap.get(topic);
            ArrayList<Long> tmpQueueOffsets = new ArrayList<>(queueOffsets);
            topicMap.put(topic, tmpQueueOffsets);
        }

        tmp.setTopicOffsetMap(topicMap);
    }

    private void deepCopyCommitOffsetMap(Offset tmp) {
        ConcurrentMap<Integer, Long> commitOffsetMap
            = new ConcurrentHashMap<>(this.commitOffsetMap.size());

        for (Integer shardId : this.commitOffsetMap.keySet()) {
            Long offset = this.commitOffsetMap.get(shardId);
            commitOffsetMap.put(shardId, offset);
        }

        tmp.setCommitOffsetMap(commitOffsetMap);
    }

    private void deepCopyDispatchedOffsetMap(Offset tmp) {
        ConcurrentMap<Integer, Long> dispatchedOffsetMap
            = new ConcurrentHashMap<>(this.dispatchedOffsetMap.size());

        for (Integer shardId : this.dispatchedOffsetMap.keySet()) {
            Long offset = this.dispatchedOffsetMap.get(shardId);
            dispatchedOffsetMap.put(shardId, offset);
        }

        tmp.setDispatchedOffsetMap(dispatchedOffsetMap);
    }

}
