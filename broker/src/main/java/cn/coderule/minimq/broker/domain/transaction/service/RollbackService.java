package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RollbackService {
    private final MessageService messageService;

    public RollbackService(MessageService messageService) {
        this.messageService = messageService;
    }

    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        MessageBO messageBO = messageService.getMessage(request);
        messageService.deletePrepareMessage(request, messageBO);
        return CommitResult.successFuture();
    }
}
