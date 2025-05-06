package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.domain.domain.model.consumer.consume.AckResult;
import cn.coderule.minimq.domain.domain.model.consumer.consume.PopResult;
import cn.coderule.minimq.rpc.broker.protocol.consumer.ConsumerInfo;
import cn.coderule.minimq.rpc.broker.protocol.consumer.request.AckRequest;
import cn.coderule.minimq.rpc.broker.protocol.consumer.request.InvisibleRequest;
import cn.coderule.minimq.rpc.broker.protocol.consumer.request.PopRequest;
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
