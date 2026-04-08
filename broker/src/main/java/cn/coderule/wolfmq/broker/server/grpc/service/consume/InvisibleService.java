package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import cn.coderule.wolfmq.broker.api.ConsumerController;
import cn.coderule.wolfmq.broker.server.grpc.converter.InvisibleConverter;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InvisibleService {
    private final ConsumerController consumerController;

    public InvisibleService(ConsumerController consumerController) {
        this.consumerController = consumerController;
    }

    public CompletableFuture<ChangeInvisibleDurationResponse> changeInvisible(RequestContext context, ChangeInvisibleDurationRequest request) {
        CompletableFuture<ChangeInvisibleDurationResponse> future = new CompletableFuture<>();

        try {
            InvisibleRequest invisibleRequest = InvisibleConverter.toInvisibleRequest(context, request);

            return consumerController.changeInvisible(invisibleRequest)
                .thenApply(InvisibleConverter::toResponse);

        } catch (Throwable t) {
            future.completeExceptionally(t);
        }

        return future;
    }

}
