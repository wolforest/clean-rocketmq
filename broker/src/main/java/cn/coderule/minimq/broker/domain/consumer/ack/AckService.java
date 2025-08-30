package cn.coderule.minimq.broker.domain.consumer.ack;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.ack.AckConverter;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.ack.store.AckMessage;
import cn.coderule.minimq.rpc.store.facade.MQFacade;
import java.util.concurrent.CompletableFuture;

public class AckService {
    private final BrokerConfig brokerConfig;
    private final MQFacade mqStore;

    public AckService(BrokerConfig brokerConfig, MQFacade mqStore) {
        this.brokerConfig = brokerConfig;
        this.mqStore = mqStore;
    }

    public CompletableFuture<AckResult> ack(AckRequest request) {
        AckMessage ackMessage = AckConverter.toAckMessage(request);
        mqStore.ack(ackMessage);

        return null;
    }
}
