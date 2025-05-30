package cn.coderule.minimq.broker.domain.relay;

import cn.coderule.minimq.broker.domain.transaction.TransactionData;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumeMessageDirectlyResult;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumerRunningInfo;
import cn.coderule.minimq.rpc.broker.protocol.header.CheckTransactionStateRequestHeader;
import cn.coderule.minimq.rpc.broker.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import cn.coderule.minimq.rpc.broker.protocol.header.GetConsumerRunningInfoRequestHeader;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<RelayResult<ConsumerRunningInfo>> getConsumerInfo(
        RequestContext context,
        RpcCommand command,
        GetConsumerRunningInfoRequestHeader header
    );

    CompletableFuture<RelayResult<ConsumeMessageDirectlyResult>> consumeMessage(
        RequestContext context,
        RpcCommand command,
        ConsumeMessageDirectlyResultRequestHeader header
    );

    RelayData<TransactionData, Void> checkTransaction(
        RequestContext context,
        RpcCommand command,
        CheckTransactionStateRequestHeader header,
        MessageBO messageBO
    );
}
