package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.AckResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class InvisibleService {
    public CompletableFuture<AckResult> changeInvisible(RequestContext context, InvisibleRequest request) {
        return null;
    }
}
