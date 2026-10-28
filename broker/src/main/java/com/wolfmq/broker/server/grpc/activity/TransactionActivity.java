package com.wolfmq.broker.server.grpc.activity;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import apache.rocketmq.v2.Status;
import com.wolfmq.broker.server.grpc.interceptor.PipelineExecutor;
import com.wolfmq.broker.server.grpc.service.TransactionService;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.rpc.common.grpc.activity.ActivityHelper;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

public class TransactionActivity {
    private final ThreadPoolExecutor executor;
    private final PipelineExecutor pipelineExecutor;

    private TransactionService transactionService;

    public TransactionActivity(ThreadPoolExecutor executor, PipelineExecutor pipelineExecutor) {
        this.executor = executor;
        this.pipelineExecutor = pipelineExecutor;
    }

    public void inject(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void submit(
        RequestContext context,
        EndTransactionRequest request,
        StreamObserver<EndTransactionResponse> responseObserver
    ) {
        ActivityHelper<EndTransactionRequest, EndTransactionResponse> helper
            = getActivityHelper(context, request, responseObserver);

        try {
            pipelineExecutor.execute(context, request);

            Runnable task = ()
                -> transactionService.submit(context, request)
                .whenComplete(helper::writeResponse);
            this.executor.submit(helper.createTask(task));
        } catch (Throwable t) {
            helper.writeResponse(null, t);
        }
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
