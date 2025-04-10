package cn.coderule.minimq.domain.domain.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Data;

@Data
public class DataVersion implements Serializable {
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

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataVersion version = (DataVersion) o;

        if (getStateVersion() != version.getStateVersion())
            return false;
        if (getTimestamp() != version.getTimestamp())
            return false;

        if (counter != null && version.counter != null) {
            return counter.longValue() == version.counter.longValue();
        }

        return null == counter && null == version.counter;

    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(getStateVersion());
        result = 31 * result + Long.hashCode(getTimestamp());
        if (null != counter) {
            long l = counter.get();
            result = 31 * result + Long.hashCode(l);
        }
        return result;
    }

}
