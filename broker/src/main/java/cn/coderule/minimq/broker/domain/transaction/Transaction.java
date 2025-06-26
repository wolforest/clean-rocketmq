package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.broker.domain.transaction.service.CommitService;
import cn.coderule.minimq.broker.domain.transaction.service.PrepareService;
import cn.coderule.minimq.broker.domain.transaction.service.SubscribeService;
import cn.coderule.minimq.domain.domain.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.CommitRequest;
import java.util.concurrent.CompletableFuture;

public class Transaction {

    private SubscribeService subscribeService;
    private PrepareService prepareService;
    private CommitService commitService;

    public void subscribe(RequestContext context, String topicName, String groupName) {
        subscribeService.subscribe(context, topicName, groupName);
    }

    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        return prepareService.prepare(context, messageBO);
    }

    public CompletableFuture<Object> commit(CommitRequest request) {
        return commitService.commit(request);
    }
}
