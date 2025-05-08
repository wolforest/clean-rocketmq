package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.domain.domain.dto.response.AckResult;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.dto.request.ConsumerInfo;
import cn.coderule.minimq.domain.domain.dto.request.AckRequest;
import cn.coderule.minimq.domain.domain.dto.request.InvisibleRequest;
import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import java.util.concurrent.CompletableFuture;

public class Consumer  {
    public boolean register(ConsumerInfo consumerInfo) {
        return true;
    }

    public boolean unregister(ConsumerInfo consumerInfo) {
        return true;
    }

    public CompletableFuture<PopResult> popMessage(PopRequest request) {
        return null;
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        return null;
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        return null;
    }

}
