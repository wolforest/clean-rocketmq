package cn.coderule.minimq.domain.service.store.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Offset implements Serializable {
    private volatile Long commitLogOffset = null;
    private volatile Long dispatchedOffset = null;
    private volatile Long indexOffset = null;

    @Setter
    private ConcurrentMap<String, ArrayList<Long>> topicOffsetMap = new ConcurrentHashMap<>(16);

    public void setCommitLogOffset(long commitLogOffset) {
        if (commitLogOffset < this.commitLogOffset) {
            return;
        }

        this.commitLogOffset = commitLogOffset;
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
        ConcurrentMap<String, ArrayList<Long>> tmpMap = new ConcurrentHashMap<>(topicOffsetMap.size());

        for (String topic : topicOffsetMap.keySet()) {
            ArrayList<Long> queueOffsets = topicOffsetMap.get(topic);
            ArrayList<Long> tmpQueueOffsets = new ArrayList<>(queueOffsets);
            tmpMap.put(topic, tmpQueueOffsets);
        }

        tmp.setTopicOffsetMap(tmpMap);

        return tmp;
    }
}
