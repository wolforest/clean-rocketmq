package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    private final MessageService messageService;
    private final MessageFactory messageFactory;

    public CommitService(MessageService messageService, MessageFactory messageFactory) {
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

        messageService.deletePrepareMessage(prepareMessage);
        return toCommitResult(enqueueResult);
    }

    private CompletableFuture<CommitResult> toCommitResult(EnqueueResult enqueueResult) {
        CommitResult result = CommitResult.builder()
            .responseCode(enqueueResult.isSuccess() ? 1 : -1)
            .build();

        return CompletableFuture.completedFuture(result);
    }
}
