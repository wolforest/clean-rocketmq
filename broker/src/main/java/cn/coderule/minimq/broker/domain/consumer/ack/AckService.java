package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.pop.ack.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.pop.ack.AckResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class AckService {
    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return null;
    }
}
