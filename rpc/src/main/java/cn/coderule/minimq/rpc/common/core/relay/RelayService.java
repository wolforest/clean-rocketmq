package cn.coderule.minimq.rpc.common.core.relay;

import cn.coderule.minimq.rpc.broker.protocol.body.ConsumeMessageDirectlyResult;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumerRunningInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<RelayResult<ConsumerRunningInfo>> getConsumerInfo(
        RequestContext context,
        ConsumerRequest request
    );

    CompletableFuture<RelayResult<ConsumeMessageDirectlyResult>> consumeMessage(
        RequestContext context,
        ConsumerRequest request
    );

    RelayData<TransactionResult, Void> checkTransaction(
        RequestContext context,
        TransactionRequest request
    );
}
