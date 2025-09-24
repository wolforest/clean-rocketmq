package cn.coderule.minimq.rpc.common.core.relay;

import cn.coderule.minimq.rpc.common.core.relay.request.ConsumeRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumeResult;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumerResult;
import cn.coderule.minimq.rpc.common.core.relay.response.RelayResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.core.relay.response.TransactionResult;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<Result<ConsumerResult>> getConsumerInfo(
        ConsumerRequest request
    );

    CompletableFuture<Result<ConsumeResult>> consumeMessage(
        ConsumeRequest request
    );

    RelayResult<TransactionResult, Void> checkTransaction(
        TransactionRequest request
    );
}
