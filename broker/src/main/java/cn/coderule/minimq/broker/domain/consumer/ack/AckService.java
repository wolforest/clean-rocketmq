package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class AckService {
    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return null;
    }
}
