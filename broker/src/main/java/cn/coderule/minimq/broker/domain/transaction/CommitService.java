package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.domain.domain.model.consumer.CommitRequest;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    public CompletableFuture<Object> commit(CommitRequest request) {
        return null;
    }
}
