package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Set;

public class MessageService {
    private final BrokerConfig brokerConfig;

    private final SocketAddress host;

    public MessageService(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;

        this.host = new InetSocketAddress(
            brokerConfig.getHost(),
            brokerConfig.getPort()
        );
    }

    public long getConsumeOffset(MessageQueue mq) {
        return 0;
    }

    public Set<MessageQueue> getMessageQueues(String storeGroup, String topic) {
        return null;
    }

    public DequeueResult getPrepareMessage(String storeGroup, int queueId, long offset, int num) {
        return null;
    }



}
