package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.message.MessageBO;
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

    public CommitService(TransactionConfig transactionConfig, CommitBuffer commitBuffer, MessageService messageService) {
        this.transactionConfig = transactionConfig;
        this.commitBuffer = commitBuffer;
        this.messageService = messageService;
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        MessageBO messageBO = messageService.getMessage(request);


        return null;
    }
}
