package cn.coderule.minimq.rpc.common.grpc.channel;

import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumeRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumeResult;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumerResult;
import cn.coderule.minimq.rpc.common.core.relay.response.RelayResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.core.relay.response.TransactionResult;
import java.util.concurrent.CompletableFuture;

public class GrpcRelayService implements RelayService {
    private final GrpcChannel channel;

    public GrpcRelayService(GrpcChannel channel) {
        this.channel = channel;
    }

    @Override
    public CompletableFuture<Result<ConsumerResult>> getConsumerInfo(ConsumerRequest request) {
        return null;
    }

    @Override
    public CompletableFuture<Result<ConsumeResult>> consumeMessage(ConsumeRequest request) {
        return null;
    }

    @Override
    public RelayResult<TransactionResult, Void> checkTransaction(TransactionRequest request) {
        return null;
    }
}
