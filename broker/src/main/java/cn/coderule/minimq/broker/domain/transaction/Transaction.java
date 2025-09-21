package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.domain.transaction.receipt.Receipt;
import cn.coderule.minimq.broker.domain.transaction.receipt.ReceiptRegistry;
import cn.coderule.minimq.broker.domain.transaction.service.CommitService;
import cn.coderule.minimq.broker.domain.transaction.service.PrepareService;
import cn.coderule.minimq.broker.domain.transaction.service.RollbackService;
import cn.coderule.minimq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.minimq.domain.core.enums.TransactionType;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;

public class Transaction {
    private ReceiptRegistry receiptRegistry;
    private SubscribeService subscribeService;
    private PrepareService prepareService;
    private CommitService commitService;
    private RollbackService rollbackService;

    public void subscribe(RequestContext context, String topicName, String groupName) {
        subscribeService.subscribe(context, topicName, groupName);
    }

    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        return prepareService.prepare(context, messageBO);
    }

    public CompletableFuture<CommitResult> submit(SubmitRequest request) {
        getReceipt(request);

        if (request.getTransactionType() == TransactionType.COMMIT) {
            return commit(request);
        }

        return rollback(request);
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        return commitService.commit(request);
    }

    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        return rollbackService.rollback(request);
    }

    private void getReceipt(SubmitRequest request) {
        Receipt receipt = receiptRegistry.poll(
            request.getProducerGroup(),
            request.getTransactionId()
        );

        if (receipt == null) {
            throw new InvalidRequestException(
                InvalidCode.INVALID_TRANSACTION_ID,
                "can't find transaction receipt"
            );
        }

        request.setStoreGroup(receipt.getStoreGroup());
        request.setMessageId(receipt.getMessageId());
        request.setCommitOffset(receipt.getCommitOffset());
        request.setQueueOffset(receipt.getQueueOffset());
    }
}
