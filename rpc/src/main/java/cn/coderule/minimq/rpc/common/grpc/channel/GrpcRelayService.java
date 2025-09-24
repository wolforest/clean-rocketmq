package cn.coderule.minimq.rpc.common.grpc.channel;

import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.RecoverOrphanedTransactionCommand;
import apache.rocketmq.v2.TelemetryCommand;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.rpc.common.core.relay.RelayService;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumeRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.ConsumerRequest;
import cn.coderule.minimq.rpc.common.core.relay.request.TransactionRequest;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumeResult;
import cn.coderule.minimq.rpc.common.core.relay.response.ConsumerResult;
import cn.coderule.minimq.rpc.common.core.relay.response.Result;
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
    public CompletableFuture<Void> checkTransaction(TransactionRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        TelemetryCommand command = buildCommand(request.getMessageBO());

        try {
            channel.writeTelemetryCommand(command);
        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

    private TelemetryCommand buildCommand(MessageBO messageBO) {
        Message message = GrpcConverter.getInstance().buildMessage(messageBO);

        RecoverOrphanedTransactionCommand transactionCommand = RecoverOrphanedTransactionCommand
            .newBuilder()
            .setTransactionId(messageBO.getTransactionId())
            .setMessage(message)
            .build();

        return TelemetryCommand.newBuilder()
            .setRecoverOrphanedTransactionCommand(transactionCommand)
            .build();
    }
}
