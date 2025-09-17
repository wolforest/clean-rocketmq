package cn.coderule.minimq.domain.domain.timer.state;

import cn.coderule.minimq.domain.domain.meta.DataVersion;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class TimerCheckpoint implements Serializable {
    private final DataVersion dataVersion;

    private String storeGroup;

    private volatile long lastReadTimeMs = 0L;
    private volatile long lastTimerLogFlushPos = 0L;
    private volatile long lastTimerQueueOffset = 0L;
    private volatile long masterTimerQueueOffset = 0L;

    public TimerCheckpoint() {
        this.dataVersion = new DataVersion();
    }

    public byte[] toBytes() {
        return TimerCheckpoint.encode(this).array();
    }

    public static ByteBuffer encode(TimerCheckpoint another) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(56);
        byteBuffer.putLong(another.getLastReadTimeMs());
        byteBuffer.putLong(another.getLastTimerLogFlushPos());
        byteBuffer.putLong(another.getLastTimerQueueOffset());
        byteBuffer.putLong(another.getMasterTimerQueueOffset());
        // new add to record dataVersion
        byteBuffer.putLong(another.getDataVersion().getStateVersion());
        byteBuffer.putLong(another.getDataVersion().getTimestamp());
        byteBuffer.putLong(another.getDataVersion().getCounter().get());
        byteBuffer.flip();
        return byteBuffer;
    }

    public static TimerCheckpoint decode(ByteBuffer byteBuffer) {
        TimerCheckpoint tmp = new TimerCheckpoint();
        tmp.setLastReadTimeMs(byteBuffer.getLong());
        tmp.setLastTimerLogFlushPos(byteBuffer.getLong());
        tmp.setLastTimerQueueOffset(byteBuffer.getLong());
        tmp.setMasterTimerQueueOffset(byteBuffer.getLong());
        // new add to record dataVersion
        if (byteBuffer.hasRemaining()) {
            tmp.getDataVersion().setStateVersion(byteBuffer.getLong());
            tmp.getDataVersion().setTimestamp(byteBuffer.getLong());
            tmp.getDataVersion().setCounter(new AtomicLong(byteBuffer.getLong()));
        }
        return tmp;
    }
}
