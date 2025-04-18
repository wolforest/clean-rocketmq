package cn.coderule.minimq.rpc.common.grpc.activity;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.core.GrpcTask;
import cn.coderule.minimq.rpc.common.grpc.core.ResponseBuilder;
import io.grpc.stub.StreamObserver;
import java.util.function.Function;

public class ActivityHelper<REQ, RESP> {
    private final REQ request;
    private final StreamObserver<RESP> responseObserver;

    private final RequestContext context;
    private final Function<Status, RESP> statusToResponse;

    public ActivityHelper(REQ request, StreamObserver<RESP> responseObserver) {
        this(null, request, responseObserver);
    }

    public ActivityHelper(RequestContext context, REQ request, StreamObserver<RESP> responseObserver) {
        this(context, request, responseObserver, null);
    }

    public ActivityHelper(RequestContext context, REQ request, StreamObserver<RESP> responseObserver, Function<Status, RESP> statusToResponse) {
        this.context = context;
        this.request = request;
        this.responseObserver = responseObserver;
        this.statusToResponse = statusToResponse;
    }

    public GrpcTask<REQ, RESP> createTask(Runnable runnable) {
        RESP errorResponse = statusToResponse.apply(getFlowControlStatus());

        return new GrpcTask<>(
            runnable,
            context,
            request,
            responseObserver,
            errorResponse
        );
    }

    public void writeResponse(Object response, Throwable t) {

    }

    private Status getFlowControlStatus() {
        return ResponseBuilder.getInstance().buildStatus(Code.TOO_MANY_REQUESTS, "flow limit");
    }

    private Status exceptionToStatus(Throwable e) {
        return ResponseBuilder.getInstance().buildStatus(e);
    }

}
