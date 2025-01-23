package com.wolf.minimq.broker.api;

import com.wolf.minimq.broker.server.vo.RequestContext;
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
    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return null;
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return null;
    }
}
