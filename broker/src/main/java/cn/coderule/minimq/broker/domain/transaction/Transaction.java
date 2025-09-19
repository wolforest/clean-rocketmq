package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.domain.transaction.service.CommitService;
import cn.coderule.minimq.broker.domain.transaction.service.PrepareService;
import cn.coderule.minimq.broker.domain.transaction.service.RollbackService;
import cn.coderule.minimq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;

public class Transaction {

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
        return null;
    }

    public CompletableFuture<CommitResult> commit(SubmitRequest request) {
        return commitService.commit(request);
    }

    public CompletableFuture<CommitResult> rollback(SubmitRequest request) {
        return rollbackService.rollback(request);
    }
    
}
