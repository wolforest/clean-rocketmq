package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollbackService {
    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        return null;
    }
}
