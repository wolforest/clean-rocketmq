package cn.coderule.minimq.broker.server.grpc.service.channel;

import apache.rocketmq.v2.HeartbeatRequest;
import apache.rocketmq.v2.HeartbeatResponse;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class HeartbeatService {

    public CompletableFuture<HeartbeatResponse> heartbeat(RequestContext context, HeartbeatRequest request) {
        CompletableFuture<HeartbeatResponse> future = new CompletableFuture<>();

        return future;
    }
}
