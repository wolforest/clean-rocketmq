package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.server.context.RequestContext;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class ActivityHelper {
    public static Status getFlowControlStatus() {
        return null;
    }

    public static Status exceptionToStatus(Throwable e) {
        return null;
    }

    public static <REQ, RESP> void submit(RequestContext context, REQ request, StreamObserver<RESP> responseObserver, ExecutorService executor, Runnable task, Function<Status, RESP> statusToResponse) {
    }

    public static <REQ, RESP> void writeResponse(RequestContext context, REQ request, Object response, ExecutorService executor, Throwable t, StreamObserver<RESP> responseObserver, Function<Status, RESP> statusToResponse) {

    }

}
