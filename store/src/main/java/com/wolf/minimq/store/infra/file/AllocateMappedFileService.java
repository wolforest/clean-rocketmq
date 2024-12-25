package com.wolf.minimq.store.infra.file;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ServiceThread;
import com.wolf.common.util.lang.ThreadUtil;
import com.wolf.minimq.domain.config.StoreConfig;
import com.wolf.minimq.domain.service.store.infra.MappedFile;
import com.wolf.minimq.store.infra.memory.TransientPool;
import com.wolf.minimq.store.server.StoreContext;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllocateMappedFileService extends ServiceThread implements Lifecycle {
    private static final int WAIT_TIMEOUT = 1000 * 5;

    private final ConcurrentMap<String, AllocateRequest> requestTable = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<AllocateRequest> requestQueue = new PriorityBlockingQueue<>();

    private final StoreConfig storeConfig;
    private final TransientPool transientPool;

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
        return null;
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
            isSuccess = true;
        } catch (InterruptedException e) {
            log.warn("{} interrupted, possibly by shutdown.", this.getServiceName());
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
        MappedFile mappedFile;
        if (storeConfig.isEnableTransientPool()) {
            TransientPool pool = StoreContext.getBean(TransientPool.class);
            mappedFile = new DefaultMappedFile(req.getFilePath(), req.getFileSize(), pool);
        } else {
            mappedFile = new DefaultMappedFile(req.getFilePath(), req.getFileSize());
        }
        return mappedFile;
    }

    private void handleAllocateIOException(IOException e, AllocateRequest req) {
        log.warn("{} service has exception. ", this.getServiceName(), e);
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
