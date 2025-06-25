package cn.coderule.minimq.domain.domain.model.consumer.running;

public class PopProcessQueueInfo {
    private int waitAckCount;
    private boolean droped;
    private long lastPopTimestamp;


    public int getWaitAckCount() {
        return waitAckCount;
    }


    public void setWaitAckCount(int waitAckCount) {
        this.waitAckCount = waitAckCount;
    }


    public boolean isDroped() {
        return droped;
    }


    public void setDroped(boolean droped) {
        this.droped = droped;
    }


    public long getLastPopTimestamp() {
        return lastPopTimestamp;
    }


    public void setLastPopTimestamp(long lastPopTimestamp) {
        this.lastPopTimestamp = lastPopTimestamp;
    }

    @Override
    public String toString() {
        return "PopProcessQueueInfo [waitAckCount:" + waitAckCount +
                ", droped:" + droped + ", lastPopTimestamp:" + lastPopTimestamp + "]";
    }
}
