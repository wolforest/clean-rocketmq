package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.domain.domain.dto.response.AckResult;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.request.AckRequest;
import cn.coderule.minimq.domain.domain.dto.request.InvisibleRequest;
import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class Consumer  {
    public boolean register(ConsumerInfo consumerInfo) {
        return true;
    }

    public boolean unregister(ConsumerInfo consumerInfo) {
        return true;
    }

    public CompletableFuture<PopResult> popMessage(RequestContext context, PopRequest request) {
        return null;
    }

    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return null;
    }

    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return null;
    }

}
