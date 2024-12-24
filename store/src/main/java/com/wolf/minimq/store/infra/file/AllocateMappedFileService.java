package com.wolf.minimq.store.infra.file;

import com.wolf.common.convention.service.Lifecycle;
import com.wolf.common.lang.concurrent.ServiceThread;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AllocateMappedFileService extends ServiceThread implements Lifecycle {
    private static final int WAIT_TIMEOUT = 1000 * 5;

    private final ConcurrentMap<String, AllocateRequest> requestTable = new ConcurrentHashMap<>();
    private final PriorityBlockingQueue<AllocateRequest> requestQueue = new PriorityBlockingQueue<>();
    private volatile boolean hasException = false;

    @Override
    public String getServiceName() {
        return AllocateMappedFileService.class.getSimpleName();
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
    public void run() {

    }

    @Override
    public void shutdown() {
        super.shutdown(true);

        for (AllocateRequest req : this.requestTable.values()) {
            if (req.mappedFile == null) {
                continue;
            }

            log.info("delete pre allocated mapped file, {}", req.mappedFile.getFileName());
            //req.mappedFile.destroy(1000);
        }
    }
}
