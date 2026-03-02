package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * this is the gateway of module producer
 */
@Slf4j
public class Producer {
    private final EnqueueService enqueueService;

    private final ProducerRegistry producerRegistry;

    public Producer(EnqueueService sender, ProducerRegistry register) {
        this.enqueueService = sender;
        this.producerRegistry = register;
    }

    public void register(RequestContext context, String groupName, ClientChannelInfo channelInfo) {
        producerRegistry.register(groupName, channelInfo);
    }

    public void unregister(RequestContext context, String groupName, ClientChannelInfo channelInfo) {
        producerRegistry.unregister(groupName, channelInfo);
    }

    public void scanIdleChannels() {
        producerRegistry.scanIdleChannels();
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return enqueueService.enqueue(context, messageBO);
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return enqueueService.enqueue(context, messageList);
    }



}
