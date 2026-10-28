package com.wolfmq.broker.domain.producer;

import com.wolfmq.domain.server.ClientChannelInfo;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.domain.mq.dto.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * this is the gateway of module producer
 */
@Slf4j
public class Producer {
    private final EnqueueService enqueueService;

    private final ProducerManager producerManager;

    public Producer(EnqueueService sender, ProducerManager register) {
        this.enqueueService = sender;
        this.producerManager = register;
    }

    public void register(RequestContext context, String groupName, ClientChannelInfo channelInfo) {
        producerManager.register(groupName, channelInfo);
    }

    public void unregister(RequestContext context, String groupName, ClientChannelInfo channelInfo) {
        producerManager.unregister(groupName, channelInfo);
    }

    public void scanIdleChannels() {
        producerManager.scanIdleChannels();
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return enqueueService.enqueue(context, messageBO);
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return enqueueService.enqueue(context, messageList);
    }



}
