package cn.coderule.minimq.rpc.common.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.ResponseBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class ActivityHelper {
    public static Status getFlowControlStatus() {
        return ResponseBuilder.getInstance().buildStatus(Code.TOO_MANY_REQUESTS, "flow limit");
    }

    public static Status exceptionToStatus(Throwable e) {
        return ResponseBuilder.getInstance().buildStatus(e);
    }

    public static <REQ, RESP> void submit(RequestContext context, REQ request, StreamObserver<RESP> responseObserver,
        ExecutorService executor, Runnable task, Function<Status, RESP> statusToResponse) {
    }

    public static <REQ, RESP> void writeResponse(RequestContext context, REQ request, Object response, ExecutorService executor, Throwable t, StreamObserver<RESP> responseObserver, Function<Status, RESP> statusToResponse) {

    }

}
