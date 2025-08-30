package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

public class AckService {
    private BrokerConfig brokerConfig;
    private MQFacade mqStore;

    public CompletableFuture<AckResult> ack(AckRequest request) {
        return null;
    }
}
