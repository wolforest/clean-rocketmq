package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.transaction.Transaction;
import java.util.concurrent.CompletableFuture;

public class TransactionController {
    private final Transaction transaction;

    public TransactionController(Transaction transaction) {
        this.transaction = transaction;
    }

    public CompletableFuture<Object> commit() {
        return null;
    }
}
