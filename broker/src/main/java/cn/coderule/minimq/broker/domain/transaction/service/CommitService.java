package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitService {
    private final TransactionConfig transactionConfig;
    private final CommitBuffer commitBuffer;
    private final MessageService messageService;
    private final MessageFactory messageFactory;

    public CommitService(
        TransactionConfig transactionConfig,
        CommitBuffer commitBuffer,
        MessageService messageService,
        MessageFactory messageFactory
    ) {
        this.transactionConfig = transactionConfig;
        this.commitBuffer = commitBuffer;
        this.messageService = messageService;
        this.messageFactory = messageFactory;
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        MessageBO prepareMessage = messageService.getMessage(request);
        MessageBO commitMessage = messageFactory.createCommitMessage(prepareMessage);

        EnqueueResult enqueueResult = messageService.enqueueCommitMessage(request, commitMessage);
        messageService.deletePrepareMessage(prepareMessage);

        return toCommitResult(enqueueResult);
    }

    private CompletableFuture<CommitResult> toCommitResult(EnqueueResult enqueueResult) {
        return null;
    }
}
