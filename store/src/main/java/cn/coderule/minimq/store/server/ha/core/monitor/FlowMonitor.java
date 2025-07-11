package cn.coderule.minimq.store.server.ha.core.monitor;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlowMonitor extends ServiceThread {
    private final AtomicLong transferredByte = new AtomicLong(0L);
    @Getter
    private volatile long transferredBytePerSecond;
    protected StoreConfig storeConfig;

    public FlowMonitor(StoreConfig storeConfig) {
        this.storeConfig = storeConfig;
    }

    @Override
    public String getServiceName() {
        return FlowMonitor.class.getSimpleName();
    }

    @Override
    public void run() {
        while (!this.isStopped()) {
            this.await(1_000);
            this.calculateSpeed();
        }
    }

    public void calculateSpeed() {
        this.transferredBytePerSecond = this.transferredByte.get();
        this.transferredByte.set(0);
    }

    public int getAvailableTransferByte() {
        // Flow control is not started at present
        if (storeConfig.isEnableHaFlowControl()) {
            long res = Math.max(this.getMaxTransferBytePerSecond() - this.transferredByte.get(), 0);
            return res > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) res;
        }
        return Integer.MAX_VALUE;
    }

    public void addTransferredByte(long count) {
        this.transferredByte.addAndGet(count);
    }

    protected boolean isEnableFlowControl() {
        return this.storeConfig.isEnableHaFlowControl();
    }

    public long getMaxTransferBytePerSecond() {
        return this.storeConfig.getMaxHaTransferBytesPerSecond();
    }
}
