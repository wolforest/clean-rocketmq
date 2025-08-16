package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.minimq.rpc.store.facade.ConsumeOffsetFacade;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import cn.coderule.minimq.rpc.store.facade.TopicFacade;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {

    private BrokerConfig brokerConfig;
    private InflightCounter inflightCounter;
    private QueueSelector queueSelector;

    private MQFacade mqFacade;
    private TopicFacade topicFacade;
    private ConsumeOffsetFacade consumeOffsetFacade;

    public CompletableFuture<PopResult> pop(PopRequest request) {
        MessageQueue messageQueue = queueSelector.select(request);


        return null;
    }

    private void checkConfig(PopRequest request) {

    }
}
