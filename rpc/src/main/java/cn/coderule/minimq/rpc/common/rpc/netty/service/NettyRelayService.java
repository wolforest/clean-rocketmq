package cn.coderule.minimq.rpc.common.rpc.netty.service;

import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumeRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumeResult;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumerResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
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
