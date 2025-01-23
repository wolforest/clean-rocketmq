package com.wolf.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;

public class TransactionActivity {
    private final ThreadPoolExecutor executor;

    public TransactionActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void endTransaction(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {
    }
}
