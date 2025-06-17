package cn.coderule.minimq.broker.domain.consumer.consumer;

import cn.coderule.minimq.broker.domain.consumer.ack.AckService;
import cn.coderule.minimq.broker.domain.consumer.ack.InvisibleService;
import cn.coderule.minimq.broker.domain.consumer.pop.PopService;
import cn.coderule.minimq.domain.domain.dto.response.AckResult;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.request.AckRequest;
import cn.coderule.minimq.domain.domain.dto.request.InvisibleRequest;
import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class Consumer  {

    private ConsumerRegister register;

    private PopService popService;
    private AckService ackService;
    private InvisibleService invisibleService;

    public boolean register(ConsumerInfo consumerInfo) {
        return register.register(consumerInfo);
    }

    public void unregister(ConsumerInfo consumerInfo) {
        register.unregister(consumerInfo);
    }

    public CompletableFuture<PopResult> popMessage(RequestContext context, PopRequest request) {
        return popService.pop(context, request);
    }

    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return ackService.ack(context, request);
    }

    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return invisibleService.changeInvisible(context, request);
    }

}
