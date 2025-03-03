package cn.coderule.minimq.rpc.common.netty;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyService {
    protected final Semaphore onewaySemaphore;
    protected final Semaphore asyncSemaphore;

    //protected final ConcurrentMap<>

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits, true);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits, true);
    }
}
