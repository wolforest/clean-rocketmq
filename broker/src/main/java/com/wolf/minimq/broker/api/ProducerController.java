package com.wolf.minimq.broker.api;

import apache.rocketmq.v2.SendMessageRequest;
import apache.rocketmq.v2.SendMessageResponse;
import com.wolf.minimq.broker.server.RequestContext;
import java.util.concurrent.CompletableFuture;

public class ProducerController {
    public CompletableFuture<SendMessageResponse> produce(
        RequestContext context, SendMessageRequest request) {
        return null;
    }
}
