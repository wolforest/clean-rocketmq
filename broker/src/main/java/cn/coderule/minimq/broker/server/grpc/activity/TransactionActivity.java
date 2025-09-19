package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import lombok.Setter;

public class TransactionActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private TransactionController transactionController;

    public TransactionActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void submit(
        RequestContext context,
        EndTransactionRequest request,
        StreamObserver<EndTransactionResponse> responseObserver
    ) {

    }

    private Function<Status, EndTransactionResponse> statusToResponse() {
        return status -> EndTransactionResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private ActivityHelper<EndTransactionRequest, EndTransactionResponse> getActivityHelper(
        RequestContext context,
        EndTransactionRequest request,
        StreamObserver<EndTransactionResponse> responseObserver
    ) {
        Function<Status, EndTransactionResponse> statusToResponse = statusToResponse();
        return new ActivityHelper<>(
            context,
            request,
            responseObserver,
            statusToResponse
        );
    }

}
