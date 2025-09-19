package cn.coderule.minimq.broker.server.grpc.service;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import cn.coderule.minimq.broker.api.TransactionController;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class TransactionService {
    private final TransactionController transactionController;

    public TransactionService(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public CompletableFuture<EndTransactionResponse> submit(
        RequestContext context,
        EndTransactionRequest request
    ) {
        return null;
    }
}
