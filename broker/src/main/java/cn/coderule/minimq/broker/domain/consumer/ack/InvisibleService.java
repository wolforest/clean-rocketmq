package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class InvisibleService {
    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return null;
    }
}
