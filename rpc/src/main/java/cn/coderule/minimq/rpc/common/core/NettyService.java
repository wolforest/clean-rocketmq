package cn.coderule.minimq.rpc.common.core;

import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyService {

    protected final Semaphore onewaySemaphore;
    protected final Semaphore asyncSemaphore;

    public NettyService(int onewaySemaphore, int asyncSemaphore) {
        this.onewaySemaphore = new Semaphore(onewaySemaphore, true);
        this.asyncSemaphore = new Semaphore(asyncSemaphore, true);
    }
}
