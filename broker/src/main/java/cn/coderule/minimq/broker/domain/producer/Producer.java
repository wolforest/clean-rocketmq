package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.domain.domain.model.producer.ProducerInfo;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.dto.EnqueueResult;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * this is the gateway of module producer
 */
@Slf4j
public class Producer {
    private final MessageSender messageSender;

    public Producer(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public boolean register(ProducerInfo producerInfo) {
        return true;
    }

    public boolean unregister(ProducerInfo producerInfo) {
        return true;
    }

    public CompletableFuture<EnqueueResult> produce(RequestContext context, MessageBO messageBO) {
        return messageSender.send(context, messageBO);
    }

    public CompletableFuture<List<EnqueueResult>> produce(RequestContext context, List<MessageBO> messageList) {
        return messageSender.send(context, messageList);
    }



}
