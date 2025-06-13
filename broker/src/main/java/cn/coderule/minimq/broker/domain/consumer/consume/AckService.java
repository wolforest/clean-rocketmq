package cn.coderule.minimq.broker.domain.consumer.consume;

import cn.coderule.minimq.domain.domain.dto.request.AckRequest;
import cn.coderule.minimq.domain.domain.dto.response.AckResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class AckService {
    public CompletableFuture<AckResult> ack(RequestContext context, AckRequest request) {
        return null;
    }
}
