package cn.coderule.minimq.rpc.common.netty;

import cn.coderule.common.ds.Pair;
import cn.coderule.minimq.rpc.common.core.ResponseFuture;
import cn.coderule.minimq.rpc.common.core.RpcProcessor;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyService {
    protected final Semaphore onewaySemaphore;
    protected final Semaphore asyncSemaphore;

    /**
     * response map
     * { opaque : ResponseFuture }
     */
    protected final ConcurrentMap<Integer, ResponseFuture> responseMap = new ConcurrentHashMap<>(256);
    /**
     * processor map
     * { requestCode: [RpcProcessor, ExecutorService] }
     */
    protected final HashMap<Integer, Pair<RpcProcessor, ExecutorService>> processorMap = new HashMap<>(64);
    protected Pair<RpcProcessor, ExecutorService> defaultProcessorPair;

    public NettyService(int onewaySemaphorePermits, int asyncSemaphorePermits) {
        this.onewaySemaphore = new Semaphore(onewaySemaphorePermits, true);
        this.asyncSemaphore = new Semaphore(asyncSemaphorePermits, true);
    }
}
