package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        Set<MessageQueue> result = new HashSet<>();

        if (!result.isEmpty()) {
            return result;
        }

        log.warn("no prepare message queue: storeGroup={}, topic={}", storeGroup, topic);
        return result;
    }

    public DequeueResult getPrepareMessage(String storeGroup, int queueId, long offset, int num) {
        return null;
    }



}
