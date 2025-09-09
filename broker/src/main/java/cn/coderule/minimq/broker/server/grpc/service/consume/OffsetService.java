package cn.coderule.minimq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.GetOffsetRequest;
import apache.rocketmq.v2.GetOffsetResponse;
import apache.rocketmq.v2.QueryOffsetRequest;
import apache.rocketmq.v2.QueryOffsetResponse;
import apache.rocketmq.v2.UpdateOffsetRequest;
import apache.rocketmq.v2.UpdateOffsetResponse;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import java.util.concurrent.CompletableFuture;

public class OffsetService {

    public CompletableFuture<GetOffsetResponse> getOffsetAsync(RequestContext context, GetOffsetRequest request) {
        return null;
    }

    public CompletableFuture<QueryOffsetResponse> queryOffsetAsync(RequestContext context, QueryOffsetRequest request) {
        return null;
    }

    public CompletableFuture<UpdateOffsetResponse> updateOffsetAsync(RequestContext context, UpdateOffsetRequest request) {
        return null;
    }


}
