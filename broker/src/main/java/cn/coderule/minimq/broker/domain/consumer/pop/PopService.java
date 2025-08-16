package cn.coderule.minimq.broker.domain.consumer.pop;

import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopRequest;
import cn.coderule.minimq.domain.domain.consumer.consume.pop.PopResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PopService {

    private InflightCounter inflightCounter;
    private QueueSelector queueSelector;

    public CompletableFuture<PopResult> pop(PopRequest request) {
        MessageQueue messageQueue = queueSelector.select(request);


        return null;
    }
}
