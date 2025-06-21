package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import java.util.concurrent.CompletableFuture;

public class Transaction {

    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        return null;
    }

    public void subscribe(RequestContext context, String topicName, String groupName) {

    }
}
