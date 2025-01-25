package com.wolf.minimq.broker.api;

import com.wolf.minimq.broker.domain.transaction.Transaction;
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
