package com.wolfmq.rpc.common.core.relay;

import com.wolfmq.rpc.common.core.relay.request.ConsumeRequest;
import com.wolfmq.rpc.common.core.relay.request.ConsumerRequest;
import com.wolfmq.rpc.common.core.relay.request.TransactionRequest;
import com.wolfmq.rpc.common.core.relay.response.ConsumeResult;
import com.wolfmq.rpc.common.core.relay.response.ConsumerResult;
import com.wolfmq.rpc.common.core.relay.response.RelayResult;
import com.wolfmq.rpc.common.core.relay.response.Result;
import com.wolfmq.rpc.common.core.relay.response.TransactionResult;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<Result<ConsumerResult>> getConsumerInfo(
        ConsumerRequest request
    );

    CompletableFuture<Result<ConsumeResult>> consumeMessage(
        ConsumeRequest request
    );

    CompletableFuture<Void> checkTransaction(
        TransactionRequest request
    );
}
