package cn.coderule.minimq.rpc.common.netty.service;

import cn.coderule.minimq.rpc.common.core.invoke.ResponseFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class NettyInvoker {
    private final Semaphore onewaySemaphore;
    private final Semaphore asyncSemaphore;
    private final ConcurrentMap<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);
    private AtomicBoolean stopping = new AtomicBoolean(false);

    public NettyInvoker(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits);
    }

    public void shutdown() {
        this.stopping.set(true);
    }
}
