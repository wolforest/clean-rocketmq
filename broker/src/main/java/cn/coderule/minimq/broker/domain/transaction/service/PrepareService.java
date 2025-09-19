package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.broker.infra.store.MQStore;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueRequest;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareService {
    private final MQStore mqStore;
    private final MessageFactory messageFactory;

    public PrepareService(MessageFactory messageFactory, MQStore mqStore) {
        this.mqStore = mqStore;
        this.messageFactory = messageFactory;
    }

    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        MessageBO prepareMessage = messageFactory.createPrepareMessage(messageBO);
        EnqueueRequest request = EnqueueRequest.builder()
            .requestContext(context)
            .messageBO(prepareMessage)
            .build();

        return mqStore.enqueueAsync(request);
    }


}
