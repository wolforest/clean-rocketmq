package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareService {
    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        return null;
    }


}
