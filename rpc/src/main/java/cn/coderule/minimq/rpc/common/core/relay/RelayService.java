package cn.coderule.minimq.rpc.common.core.relay;

import cn.coderule.minimq.rpc.broker.protocol.body.ConsumeMessageDirectlyResult;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumerRunningInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.RelayResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import cn.coderule.minimq.rpc.common.core.relay.response.TransactionResult;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<Result<ConsumerRunningInfo>> getConsumerInfo(
        RequestContext context,
        ConsumerRequest request
    );

    CompletableFuture<Result<ConsumeMessageDirectlyResult>> consumeMessage(
        RequestContext context,
        ConsumerRequest request
    );

    RelayResult<TransactionResult, Void> checkTransaction(
        RequestContext context,
        TransactionRequest request
    );
}
