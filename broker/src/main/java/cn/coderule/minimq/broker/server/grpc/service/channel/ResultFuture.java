package cn.coderule.minimq.broker.server.grpc.service.channel;

import cn.coderule.minimq.rpc.common.core.relay.response.Result;
import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;

@Getter
public class ResultFuture<T> implements Serializable {
    private final CompletableFuture<Result<T>> future;
    public final long createTime;

    public ResultFuture(CompletableFuture<Result<T>> future) {
        this.future = future;
        this.createTime = System.currentTimeMillis();
    }
}
