package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.transaction.Transaction;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;

public class TransactionController {
    private final Transaction transaction;

    public TransactionController(Transaction transaction) {
        this.transaction = transaction;
    }

    public void subscribe(RequestContext context, String topicName, String groupName) {
        transaction.subscribe(context, topicName, groupName);
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        // validate topic
        // validate transactionId: not blank
        return transaction.commit(request);
    }

    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        return transaction.rollback(request);
    }
}
