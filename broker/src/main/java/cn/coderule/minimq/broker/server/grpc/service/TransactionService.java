package cn.coderule.minimq.broker.server.grpc.service;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import apache.rocketmq.v2.Status;
import apache.rocketmq.v2.TransactionResolution;
import apache.rocketmq.v2.TransactionSource;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.domain.core.enums.TransactionStatus;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;
import java.util.concurrent.CompletableFuture;

public class TransactionService {
    private final TransactionController transactionController;

    public TransactionService(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public CompletableFuture<EndTransactionResponse> submit(RequestContext context, EndTransactionRequest request) {
        try {
            SubmitRequest submitRequest = buildSubmitRequest(context, request);

            return transactionController.submit(submitRequest)
                .thenApply(result -> success());

        } catch (Throwable t) {
            return failure(t);
        }
    }

    private CompletableFuture<EndTransactionResponse> failure(Throwable t) {
        CompletableFuture<EndTransactionResponse> future = new CompletableFuture<>();
        future.completeExceptionally(t);
        return future;
    }

    private EndTransactionResponse success() {
        Status status = ResponseBuilder.getInstance()
            .buildStatus(Code.OK, Code.OK.name());

        return EndTransactionResponse.newBuilder()
            .setStatus(status)
            .build();
    }

    private SubmitRequest buildSubmitRequest(
        RequestContext context,
        EndTransactionRequest request
    ) {
        boolean byCheck = request.getSource().equals(TransactionSource.SOURCE_SERVER_CHECK);
        return SubmitRequest.builder()
            .requestContext(context)
            .transactionId(request.getTransactionId())
            .messageId(request.getMessageId())
            .topicName(request.getTopic().getName())
            .byCheck(byCheck)
            .status(getTransactionStatus(request))
            .build();
    }

    private TransactionStatus getTransactionStatus(EndTransactionRequest request) {
        TransactionStatus transactionStatus = TransactionStatus.UNKNOWN;
        TransactionResolution transactionResolution = request.getResolution();
        switch (transactionResolution) {
            case COMMIT:
                transactionStatus = TransactionStatus.COMMIT;
                break;
            case ROLLBACK:
                transactionStatus = TransactionStatus.ROLLBACK;
                break;
            default:
                break;
        }

        return transactionStatus;
    }
}
