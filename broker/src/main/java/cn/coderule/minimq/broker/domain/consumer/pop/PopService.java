package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.consumer.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.pop.PopResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class PopService {

    private InflightCounter inflightCounter;

    public CompletableFuture<PopResult> pop(RequestContext context, PopRequest request) {
        return null;
    }
}
