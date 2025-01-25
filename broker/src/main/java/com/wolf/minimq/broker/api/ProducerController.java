package com.wolf.minimq.broker.api;

import com.wolf.minimq.broker.domain.producer.Producer;
import com.wolf.minimq.broker.server.model.RequestContext;
import com.wolf.minimq.domain.model.bo.MessageBO;
import com.wolf.minimq.domain.model.dto.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * ProducerController
 *  - accept RequestContext and MessageBO
 *  - return EnqueueResult
 *  - for multi-protocol support
 */
public class ProducerController {
    private final Producer producer;

    public ProducerController(Producer producer) {
        this.producer = producer;
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return null;
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return null;
    }
}
