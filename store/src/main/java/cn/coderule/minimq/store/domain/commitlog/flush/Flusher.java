package cn.coderule.minimq.store.domain.commitlog.flush;

import cn.coderule.minimq.store.domain.commitlog.vo.GroupCommitRequest;
import cn.coderule.common.lang.concurrent.ServiceThread;

public abstract class Flusher extends ServiceThread {
    protected static final int RETRY_TIMES = 10;
    protected long maxOffset = 0;

    public void setMaxOffset(long maxOffset) {
        if (maxOffset > this.maxOffset) {
            this.maxOffset = maxOffset;
        }
    }

    public void addRequest(GroupCommitRequest request) {

    }
}
