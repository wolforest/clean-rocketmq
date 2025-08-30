package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

public class InvisibleService {
    private final BrokerConfig brokerConfig;
    private final MQFacade mqStore;

    public InvisibleService(BrokerConfig brokerConfig, MQFacade mqStore) {
        this.brokerConfig = brokerConfig;
        this.mqStore = mqStore;
    }

    public CompletableFuture<AckResult> changeInvisible(InvisibleRequest request) {
        return null;
    }
}
