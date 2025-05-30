package cn.coderule.minimq.domain.domain.model.consumer.pop.checkpoint;

import cn.coderule.minimq.domain.domain.constant.PopConstants;
import cn.coderule.minimq.domain.domain.model.consumer.pop.helper.PopKeyBuilder;
import java.util.concurrent.atomic.AtomicInteger;

public class PopCheckPointWrapper {
    private final int reviveQueueId;
    // -1: not stored, >=0: stored, Long.MAX: storing.
    private volatile long reviveQueueOffset;
    private final PopCheckPoint ck;
    // bits for concurrent
    private final AtomicInteger bits;
    // bit for stored buffer ak
    private final AtomicInteger toStoreBits;
    private final long nextBeginOffset;
    private final String lockKey;
    private final String mergeKey;
    /**
     * with default config, this property is useless
     */
    private final boolean justOffset;
    /**
     * flag whether check point has stored in revive queue
     */
    private volatile boolean ckStored = false;

    public PopCheckPointWrapper(int reviveQueueId, long reviveQueueOffset, PopCheckPoint point,
        long nextBeginOffset) {
        this.reviveQueueId = reviveQueueId;
        this.reviveQueueOffset = reviveQueueOffset;
        this.ck = point;
        this.bits = new AtomicInteger(0);
        this.toStoreBits = new AtomicInteger(0);
        this.nextBeginOffset = nextBeginOffset;
        this.lockKey = PopKeyBuilder.buildLockKey(point);
        this.mergeKey = PopKeyBuilder.buildKey(point);
        this.justOffset = false;
    }

    public PopCheckPointWrapper(int reviveQueueId, long reviveQueueOffset, PopCheckPoint point,
        long nextBeginOffset,
        boolean justOffset) {
        this.reviveQueueId = reviveQueueId;
        this.reviveQueueOffset = reviveQueueOffset;
        this.ck = point;
        this.bits = new AtomicInteger(0);
        this.toStoreBits = new AtomicInteger(0);
        this.nextBeginOffset = nextBeginOffset;
        this.lockKey = ck.getTopic() + PopConstants.SPLIT + ck.getCId() + PopConstants.SPLIT + ck.getQueueId();
        this.mergeKey = point.getTopic() + point.getCId() + point.getQueueId() + point.getStartOffset() + point.getPopTime() + point.getBrokerName();
        this.justOffset = justOffset;
    }

    public int getReviveQueueId() {
        return reviveQueueId;
    }

    public long getReviveQueueOffset() {
        return reviveQueueOffset;
    }

    public boolean isCkStored() {
        return ckStored;
    }

    public void setReviveQueueOffset(long reviveQueueOffset) {
        this.reviveQueueOffset = reviveQueueOffset;
    }

    public PopCheckPoint getCk() {
        return ck;
    }

    public AtomicInteger getBits() {
        return bits;
    }

    public AtomicInteger getToStoreBits() {
        return toStoreBits;
    }

    public long getNextBeginOffset() {
        return nextBeginOffset;
    }

    public String getLockKey() {
        return lockKey;
    }

    public String getMergeKey() {
        return mergeKey;
    }

    public boolean isJustOffset() {
        return justOffset;
    }

    public void setCkStored(boolean ckStored) {
        this.ckStored = ckStored;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CkWrap{");
        sb.append("rq=").append(reviveQueueId);
        sb.append(", rqo=").append(reviveQueueOffset);
        sb.append(", ck=").append(ck);
        sb.append(", bits=").append(bits);
        sb.append(", sBits=").append(toStoreBits);
        sb.append(", nbo=").append(nextBeginOffset);
        sb.append(", cks=").append(ckStored);
        sb.append(", jo=").append(justOffset);
        sb.append('}');
        return sb.toString();
    }
}

