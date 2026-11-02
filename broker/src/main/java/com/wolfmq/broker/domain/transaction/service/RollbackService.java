package com.wolfmq.broker.domain.transaction.service;

import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.transaction.SubmitRequest;
import com.wolfmq.common.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollbackService {
    private final TransactionMessageService messageService;

    public RollbackService(TransactionMessageService messageService) {
        this.messageService = messageService;
    }

    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        MessageBO messageBO = messageService.getMessage(request);
        messageService.deletePrepareMessage(request, messageBO);
        return CommitResult.successFuture();
    }
}
