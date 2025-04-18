package cn.coderule.minimq.rpc.common.grpc.activity;

import cn.coderule.minimq.rpc.common.grpc.core.GrpcTask;
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
            @SuppressWarnings({"rawtypes"})
            ActivityHelper activity = new ActivityHelper(
                grpcTask.getContext(),
                grpcTask.getRequest(),
                grpcTask.getStreamObserver()
            );

            activity.writeResponse(grpcTask.getExecuteRejectResponse(), null);
        } catch (Throwable t) {
            log.warn("write rejected error response failed", t);
        }
    }
}
