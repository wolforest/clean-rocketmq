package cn.coderule.minimq.rpc.common.grpc.core;

import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumeResult;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumerResult;
import cn.coderule.minimq.rpc.common.core.relay.response.RelayResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.core.relay.response.TransactionResult;
import java.util.concurrent.CompletableFuture;

public class GrpcRelayService implements RelayService {
    @Override
    public CompletableFuture<Result<ConsumerResult>> getConsumerInfo(RequestContext context,
        ConsumerRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Result<ConsumeResult>> consumeMessage(RequestContext context,
        ConsumerRequest request) {
        return null;
    }

    @Override
    public RelayResult<TransactionResult, Void> checkTransaction(RequestContext context, TransactionRequest request) {
        return null;
    }
}
