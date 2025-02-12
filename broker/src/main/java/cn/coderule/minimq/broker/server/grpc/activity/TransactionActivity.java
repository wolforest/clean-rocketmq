package cn.coderule.minimq.broker.server.grpc.activity;

import apache.rocketmq.v2.EndTransactionRequest;
import apache.rocketmq.v2.EndTransactionResponse;
import cn.coderule.minimq.broker.api.TransactionController;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.Setter;

public class TransactionActivity {
    private final ThreadPoolExecutor executor;

    @Setter
    private TransactionController transactionController;

    public TransactionActivity(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    public void commit(EndTransactionRequest request, StreamObserver<EndTransactionResponse> responseObserver) {
    }
}
