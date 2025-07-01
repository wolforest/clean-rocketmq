package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.transaction.DeleteBuffer;
import cn.coderule.minimq.domain.domain.transaction.CommitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    private final DeleteBuffer deleteBuffer;
    private final MessageService messageService;

    public CommitService(DeleteBuffer deleteBuffer, MessageService messageService) {
        this.deleteBuffer = deleteBuffer;
        this.messageService = messageService;
    }

    public CompletableFuture<CommitResult> commit(CommitRequest request) {
        return null;
    }
}
