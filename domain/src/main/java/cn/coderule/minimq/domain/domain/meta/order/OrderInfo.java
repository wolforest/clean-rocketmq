package cn.coderule.minimq.domain.domain.meta.order;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import com.alibaba.fastjson2.annotation.JSONField;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo implements Serializable {
    private long popTime;
    /**
     * the invisibleTime when pop message
     */
    @JSONField(name = "i")
    private Long invisibleTime;
    /**
     * offset
     * offsetList[0] is the queue offset of message
     * offsetList[i] (i > 0) is the distance between current message and offsetList[0]
     */
    @JSONField(name = "o")
    private List<Long> offsetList;
    /**
     * next visible timestamp for message
     * key: message queue offset
     */
    @JSONField(name = "ot")
    private Map<Long, Long> offsetNextVisibleTime;
    /**
     * message consumed count for offset
     * key: message queue offset
     */
    @JSONField(name = "oc")
    private Map<Long, Integer> offsetConsumedCount;
    /**
     * last consume timestamp
     */
    @JSONField(name = "l")
    private long lastConsumeTimestamp;
    /**
     * commit offset bit
     */
    @JSONField(name = "cm")
    private long commitOffsetBit;
    @JSONField(name = "a")
    private String attemptId;

    @JSONField(serialize = false, deserialize = false)
    public boolean isLocked(String attemptId, long invisibleTime) {
        if (CollectionUtil.isEmpty(offsetList)) {
            return false;
        }

        if (null != this.attemptId && this.attemptId.equals(attemptId)) {
            return false;
        }

        if (null == this.invisibleTime || this.invisibleTime <= 0) {
            this.invisibleTime = invisibleTime;
        }

        int num = offsetList.size();
        long now = System.currentTimeMillis();
        for (int i = 0; i < num; i++) {
            if (!hasNotAck(i)) {
                continue;
            }

            long nextVisibleTime = calculateNextVisibleTime(i, invisibleTime);
            if (nextVisibleTime > now) {
                return true;
            }
        }

        return false;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean hasNotAck(int offsetIndex) {
        return (commitOffsetBit & (1L << offsetIndex)) == 0;
    }

    @JSONField(serialize = false, deserialize = false)
    public long getQueueOffset(int offsetIndex) {
        return getQueueOffset(this.offsetList, offsetIndex);
    }

    @JSONField(serialize = false, deserialize = false)
    public Long getLockFreeTime() {
        if (offsetList == null || offsetList.isEmpty()) {
            return null;
        }
        int num = offsetList.size();
        int i = 0;
        long currentTime = System.currentTimeMillis();
        for (; i < num; i++) {
            if (!hasNotAck(i)) {
                continue;
            }

            if (invisibleTime == null || invisibleTime <= 0) {
                return null;
            }
            long nextVisibleTime = popTime + invisibleTime;
            if (offsetNextVisibleTime != null) {
                Long time = offsetNextVisibleTime.get(this.getQueueOffset(i));
                if (time != null) {
                    nextVisibleTime = time;
                }
            }
            if (currentTime < nextVisibleTime) {
                return nextVisibleTime;
            }
        }
        return currentTime;
    }

    @JSONField(serialize = false, deserialize = false)
    public void updateOffsetNextVisibleTime(long queueOffset, long nextVisibleTime) {
        if (this.offsetNextVisibleTime == null) {
            this.offsetNextVisibleTime = new HashMap<>();
        }
        this.offsetNextVisibleTime.put(queueOffset, nextVisibleTime);
    }

    @JSONField(serialize = false, deserialize = false)
    public long getNextOffset() {
        if (offsetList == null || offsetList.isEmpty()) {
            return -2;
        }
        int num = offsetList.size();
        int i = 0;
        for (; i < num; i++) {
            if (hasNotAck(i)) {
                break;
            }
        }
        if (i == num) {
            // all ack
            return getQueueOffset(num - 1) + 1;
        }
        return getQueueOffset(i);
    }

    @JSONField(serialize = false, deserialize = false)
    public void mergeOffsetConsumedCount(String preAttemptId, List<Long> preOffsetList, Map<Long, Integer> prevOffsetConsumedCount) {
        Map<Long, Integer> offsetConsumedCount = new HashMap<>();
        if (prevOffsetConsumedCount == null) {
            prevOffsetConsumedCount = new HashMap<>();
        }
        if (preAttemptId != null && preAttemptId.equals(this.attemptId)) {
            this.offsetConsumedCount = prevOffsetConsumedCount;
            return;
        }
        Set<Long> preQueueOffsetSet = new HashSet<>();
        for (int i = 0; i < preOffsetList.size(); i++) {
            preQueueOffsetSet.add(getQueueOffset(preOffsetList, i));
        }

        for (int i = 0; i < offsetList.size(); i++) {
            long queueOffset = this.getQueueOffset(i);
            if (!preQueueOffsetSet.contains(queueOffset)) {
                continue;
            }

            int count = 1;
            Integer preCount = prevOffsetConsumedCount.get(queueOffset);
            if (preCount != null) {
                count = preCount + 1;
            }
            offsetConsumedCount.put(queueOffset, count);
        }
        this.offsetConsumedCount = offsetConsumedCount;
    }

    private static long getQueueOffset(List<Long> offsetList, int offsetIndex) {
        if (offsetIndex == 0) {
            return offsetList.get(0);
        }
        return offsetList.get(0) + offsetList.get(offsetIndex);
    }

    private long calculateNextVisibleTime(int i, long invisibleTime) {
        long nextVisibleTime = popTime + invisibleTime;
        if (offsetNextVisibleTime == null) {
            return nextVisibleTime;
        }

        long offset = getQueueOffset(i);
        Long time = offsetNextVisibleTime.get(offset);
        if (time != null) {
            return time;
        }

        return nextVisibleTime;
    }

}
