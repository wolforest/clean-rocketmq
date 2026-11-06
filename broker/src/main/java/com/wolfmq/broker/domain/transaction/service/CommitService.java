package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.messagequeue.dto.EnqueueResult;
import com.wolfmq.common.domain.transaction.SubmitRequest;
import com.wolfmq.common.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    private final TransactionMessageService messageService;
    private final MessageFactory messageFactory;

    public CommitService(TransactionMessageService messageService, MessageFactory messageFactory) {
        this.messageService = messageService;
        this.messageFactory = messageFactory;
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        MessageBO prepareMessage = messageService.getMessage(request);
        MessageBO commitMessage = messageFactory.createCommitMessage(request, prepareMessage);

        EnqueueResult enqueueResult = messageService.enqueueCommitMessage(request, commitMessage);
        if (!enqueueResult.isSuccess()) {
            return toCommitResult(enqueueResult);
        }

        messageService.deletePrepareMessage(request, prepareMessage);
        return toCommitResult(enqueueResult);
    }

    private CompletableFuture<CommitResult> toCommitResult(EnqueueResult enqueueResult) {
        CommitResult result = CommitResult.builder()
            .responseCode(enqueueResult.isSuccess() ? 1 : -1)
            .build();

        return CompletableFuture.completedFuture(result);
    }
}
