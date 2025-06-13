package cn.coderule.minimq.broker.api;

import cn.coderule.minimq.broker.domain.consumer.consumer.Consumer;
import cn.coderule.minimq.domain.domain.dto.request.AckRequest;
import cn.coderule.minimq.domain.domain.dto.request.InvisibleRequest;
import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import cn.coderule.minimq.domain.domain.dto.response.AckResult;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class ConsumerController {
    private final Consumer consumer;

    public ConsumerController(Consumer consumer) {
        this.consumer = consumer;
    }

    public CompletableFuture<PopResult> popMessage(RequestContext context, PopRequest request) {
        return consumer.popMessage(context, request);
    }

    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return consumer.ack(context, request);
    }

    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return consumer.changeInvisible(context, request);
    }
}
