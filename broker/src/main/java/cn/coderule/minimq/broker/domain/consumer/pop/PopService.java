package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.dto.request.PopRequest;
import cn.coderule.minimq.domain.domain.dto.response.PopResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class PopService {

    private InflightCounter inflightCounter;

    public CompletableFuture<PopResult> pop(RequestContext context, PopRequest request) {
        return null;
    }
}
