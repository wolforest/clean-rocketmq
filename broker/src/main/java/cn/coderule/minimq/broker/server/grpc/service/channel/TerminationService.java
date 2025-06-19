package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.NotifyClientTerminationRequest;
import apache.rocketmq.v2.NotifyClientTerminationResponse;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class TerminationService {

    public CompletableFuture<NotifyClientTerminationResponse> terminate(RequestContext context, NotifyClientTerminationRequest request) {
        return CompletableFuture.completedFuture(null);
    }
}
