package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    private final CommitBuffer commitBuffer;
    private final MessageService messageService;

    public CommitService(CommitBuffer commitBuffer, MessageService messageService) {
        this.commitBuffer = commitBuffer;
        this.messageService = messageService;
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        return null;
    }
}
