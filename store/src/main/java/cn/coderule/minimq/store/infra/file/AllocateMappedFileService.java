package cn.coderule.minimq.store.infra.file;

import cn.coderule.minimq.store.infra.memory.TransientPool;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.minimq.domain.config.StoreConfig;
import cn.coderule.minimq.domain.service.store.infra.MappedFile;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllocateMappedFileService extends ServiceThread implements Lifecycle {
    private static final int WAIT_TIMEOUT = 1000 * 5;

    private final ConcurrentMap<String, AllocateRequest> requestTable = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<AllocateRequest> requestQueue = new PriorityBlockingQueue<>();

    private final StoreConfig storeConfig;
    private final TransientPool transientPool;
    private volatile boolean hasException = false;

    public AllocateMappedFileService(StoreConfig storeConfig, TransientPool transientPool) {
        this.storeConfig = storeConfig;
        this.transientPool = transientPool;
    }

    @Override
    public String getServiceName() {
        return AllocateMappedFileService.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            this.allocate();
        }
        log.info("{} service end", this.getServiceName());
    }

    public MappedFile enqueue(String path, String nextPath, int fileSize) {
        int limit = getEnqueueLimit();

        if (!enqueueRequest(path, fileSize, limit)) {
            return null;
        }

        limit--;
        enqueueRequest(nextPath, fileSize, limit);

        if (hasException) {
            log.warn("{} service has exception. so return null", this.getServiceName());
            return null;
        }

        return waitAndReturnMappedFile(path);
    }

    @Override
    public void shutdown() {
        super.shutdown(true);

        for (AllocateRequest req : this.requestTable.values()) {
            if (req.mappedFile == null) {
                continue;
            }

            log.info("delete pre allocated mapped file, {}", req.mappedFile.getFileName());
            req.mappedFile.destroy(1000);
        }
    }

    @Override
    public void initialize() {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public State getState() {
        return State.RUNNING;
    }

    private MappedFile waitAndReturnMappedFile(String path) {
        AllocateRequest result = this.requestTable.get(path);
        if (result == null) {
            log.error("find preallocate mmap failed, this never happen");
            return null;
        }

        try {
            boolean waitOK = result.getCountDownLatch().await(WAIT_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!waitOK) {
                log.warn("create mmap timeout {} {}", result.getFilePath(), result.getFileSize());
                return null;
            }

            this.requestTable.remove(path);
            return result.getMappedFile();
        } catch (InterruptedException e) {
            log.warn("{} service has exception. ", this.getServiceName(), e);
        }

        return null;
    }

    private boolean enqueueRequest(String path, int fileSize, int limit) {
        AllocateRequest request = new AllocateRequest(path, fileSize);
        boolean status = this.requestTable.putIfAbsent(path, request) == null;
        if (!status) {
            return true;
        }

        if (limit <= 0) {
            log.warn("[NOTIFY]TransientStorePool is not enough, so create mapped file error, " +
                "RequestQueueSize : {}, StorePoolSize: {}", this.requestQueue.size(), transientPool.availableBufferNums());
            this.requestTable.remove(path);
            return false;
        }

        this.requestQueue.offer(request);
        return true;
    }

    private int getEnqueueLimit() {
        int limit = 2;
        if (!storeConfig.isEnableTransientPool()) {
            return limit;
        }

        if (storeConfig.isFastFailIfNotExistInTransientPool()) {
            limit = transientPool.availableBufferNums() - this.requestTable.size();
        }

        return limit;
    }

    private void allocate() {
        boolean isSuccess = false;
        AllocateRequest request = null;

        try {
            request = requestQueue.take();
            if (!validateRequest(request)) {
                return;
            }

            MappedFile mappedFile = createMappedFile(request);
            request.setMappedFile(mappedFile);

            this.hasException = false;
            isSuccess = true;
        } catch (InterruptedException e) {
            log.warn("{} interrupted, possibly by shutdown.", this.getServiceName());
            this.hasException = true;
        } catch (IOException e) {
            handleAllocateIOException(e, request);
        } finally {
            countDownRequest(request, isSuccess);
        }
    }

    private boolean validateRequest(AllocateRequest request) {
        AllocateRequest expectedRequest = requestTable.get(request.getFilePath());
        if (null == expectedRequest) {
            log.warn("this mmap request expired, maybe cause timeout {} {}",
                request.getFilePath(), request.getFileSize());
            return false;
        }
        if (expectedRequest != request) {
            log.warn("never expected here,  maybe cause timeout {} {}, req:{}, expectedRequest:{}",
                request.getFilePath(), request.getFileSize(), request, expectedRequest);
            return false;
        }

        return request.getMappedFile() == null;
    }

    private MappedFile createMappedFile(AllocateRequest req) throws IOException {
        if (null != transientPool) {
            return new DefaultMappedFile(req.getFilePath(), req.getFileSize(), transientPool);
        }

        return new DefaultMappedFile(req.getFilePath(), req.getFileSize());
    }

    private void handleAllocateIOException(IOException e, AllocateRequest req) {
        log.warn("{} service has exception. ", this.getServiceName(), e);
        this.hasException = true;
        if (null == req) {
            return;
        }

        requestQueue.offer(req);
        ThreadUtil.sleep(1);
    }

    private void countDownRequest(AllocateRequest request, boolean isSuccess) {
        if (request == null) {
            return;
        }

        if (isSuccess) {
            return;
        }

        request.getCountDownLatch().countDown();
    }

}
