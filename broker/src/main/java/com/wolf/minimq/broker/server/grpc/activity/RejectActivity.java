package com.wolf.minimq.broker.server.grpc.activity;

import com.wolf.minimq.broker.server.grpc.common.GrpcTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RejectActivity implements RejectedExecutionHandler {
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (!(r instanceof GrpcTask)) {
            return;
        }

        try {
            @SuppressWarnings({"rawtypes"})
            GrpcTask grpcTask = (GrpcTask) r;
            ActivityHelper.writeResponse(
                grpcTask.getContext(),
                grpcTask.getRequest(),
                grpcTask.getExecuteRejectResponse(),
                null,
                null,
                grpcTask.getStreamObserver(),
                null);
        } catch (Throwable t) {
            log.warn("write rejected error response failed", t);
        }
    }
}
