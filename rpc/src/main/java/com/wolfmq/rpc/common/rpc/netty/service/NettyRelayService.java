package com.wolfmq.rpc.common.rpc.netty.service;

import com.wolfmq.rpc.common.core.relay.RelayService;
import com.wolfmq.rpc.common.core.relay.request.ConsumeRequest;
import com.wolfmq.rpc.common.core.relay.request.ConsumerRequest;
import com.wolfmq.rpc.common.core.relay.request.TransactionRequest;
import com.wolfmq.rpc.common.core.relay.response.ConsumeResult;
import com.wolfmq.rpc.common.core.relay.response.ConsumerResult;
import com.wolfmq.rpc.common.core.relay.response.Result;
import java.util.concurrent.CompletableFuture;

public class NettyRelayService implements RelayService {
    @Override
    public CompletableFuture<Result<ConsumerResult>> getConsumerInfo(ConsumerRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Result<ConsumeResult>> consumeMessage(ConsumeRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Void> checkTransaction(TransactionRequest request) {
        return null;
    }
}
