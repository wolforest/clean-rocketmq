package cn.coderule.minimq.rpc.common.protocol;

import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataVersion extends RpcSerializable {
    private long stateVersion = 0L;
    private long timestamp = System.currentTimeMillis();
    private AtomicLong counter = new AtomicLong(0);

    public void assign(final DataVersion dataVersion) {
        this.timestamp = dataVersion.timestamp;
        this.stateVersion = dataVersion.stateVersion;
        this.counter.set(dataVersion.counter.get());
    }

    public void nextVersion() {
        this.nextVersion(0L);
    }

    public void nextVersion(long stateVersion) {
        this.timestamp = System.currentTimeMillis();
        this.stateVersion = stateVersion;
        this.counter.incrementAndGet();
    }
}
