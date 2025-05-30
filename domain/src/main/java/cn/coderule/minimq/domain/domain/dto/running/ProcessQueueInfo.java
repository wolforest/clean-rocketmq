
package cn.coderule.minimq.domain.domain.dto.running;

import cn.coderule.common.util.lang.time.DateUtil;

public class ProcessQueueInfo {
    private long commitOffset;

    private long cachedMsgMinOffset;
    private long cachedMsgMaxOffset;
    private int cachedMsgCount;
    private int cachedMsgSizeInMiB;

    private long transactionMsgMinOffset;
    private long transactionMsgMaxOffset;
    private int transactionMsgCount;

    private boolean locked;
    private long tryUnlockTimes;
    private long lastLockTimestamp;

    private boolean droped;
    private long lastPullTimestamp;
    private long lastConsumeTimestamp;

    public long getCommitOffset() {
        return commitOffset;
    }

    public void setCommitOffset(long commitOffset) {
        this.commitOffset = commitOffset;
    }

    public long getCachedMsgMinOffset() {
        return cachedMsgMinOffset;
    }

    public void setCachedMsgMinOffset(long cachedMsgMinOffset) {
        this.cachedMsgMinOffset = cachedMsgMinOffset;
    }

    public long getCachedMsgMaxOffset() {
        return cachedMsgMaxOffset;
    }

    public void setCachedMsgMaxOffset(long cachedMsgMaxOffset) {
        this.cachedMsgMaxOffset = cachedMsgMaxOffset;
    }

    public int getCachedMsgCount() {
        return cachedMsgCount;
    }

    public void setCachedMsgCount(int cachedMsgCount) {
        this.cachedMsgCount = cachedMsgCount;
    }

    public long getTransactionMsgMinOffset() {
        return transactionMsgMinOffset;
    }

    public void setTransactionMsgMinOffset(long transactionMsgMinOffset) {
        this.transactionMsgMinOffset = transactionMsgMinOffset;
    }

    public long getTransactionMsgMaxOffset() {
        return transactionMsgMaxOffset;
    }

    public void setTransactionMsgMaxOffset(long transactionMsgMaxOffset) {
        this.transactionMsgMaxOffset = transactionMsgMaxOffset;
    }

    public int getTransactionMsgCount() {
        return transactionMsgCount;
    }

    public void setTransactionMsgCount(int transactionMsgCount) {
        this.transactionMsgCount = transactionMsgCount;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public long getTryUnlockTimes() {
        return tryUnlockTimes;
    }

    public void setTryUnlockTimes(long tryUnlockTimes) {
        this.tryUnlockTimes = tryUnlockTimes;
    }

    public long getLastLockTimestamp() {
        return lastLockTimestamp;
    }

    public void setLastLockTimestamp(long lastLockTimestamp) {
        this.lastLockTimestamp = lastLockTimestamp;
    }

    public boolean isDroped() {
        return droped;
    }

    public void setDroped(boolean droped) {
        this.droped = droped;
    }

    public long getLastPullTimestamp() {
        return lastPullTimestamp;
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        this.lastPullTimestamp = lastPullTimestamp;
    }

    public long getLastConsumeTimestamp() {
        return lastConsumeTimestamp;
    }

    public void setLastConsumeTimestamp(long lastConsumeTimestamp) {
        this.lastConsumeTimestamp = lastConsumeTimestamp;
    }

    public int getCachedMsgSizeInMiB() {
        return cachedMsgSizeInMiB;
    }

    public void setCachedMsgSizeInMiB(final int cachedMsgSizeInMiB) {
        this.cachedMsgSizeInMiB = cachedMsgSizeInMiB;
    }

    @Override
    public String toString() {
        return "ProcessQueueInfo [commitOffset=" + commitOffset + ", cachedMsgMinOffset="
            + cachedMsgMinOffset + ", cachedMsgMaxOffset=" + cachedMsgMaxOffset
            + ", cachedMsgCount=" + cachedMsgCount + ", cachedMsgSizeInMiB=" + cachedMsgSizeInMiB
            + ", transactionMsgMinOffset=" + transactionMsgMinOffset
            + ", transactionMsgMaxOffset=" + transactionMsgMaxOffset + ", transactionMsgCount="
            + transactionMsgCount + ", locked=" + locked + ", tryUnlockTimes=" + tryUnlockTimes
            + ", lastLockTimestamp=" + DateUtil.toHumanString(lastLockTimestamp) + ", droped="
            + droped + ", lastPullTimestamp=" + DateUtil.toHumanString(lastPullTimestamp)
            + ", lastConsumeTimestamp=" + DateUtil.toHumanString(lastConsumeTimestamp) + "]";
    }
}
