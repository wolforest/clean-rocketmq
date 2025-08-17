package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;

@Data
public class PopContext implements Serializable {
    private final BrokerConfig brokerConfig;
    private final long popTime;
    private final PopRequest popRequest;
    private final MessageQueue messageQueue;

    private Topic topic;
    private String retryTopicName;

    private int reviveQueueId;
    private int retryRandom;

    public PopContext(BrokerConfig brokerConfig, PopRequest popRequest, MessageQueue messageQueue) {
        this.brokerConfig = brokerConfig;
        this.popRequest = popRequest;
        this.messageQueue = messageQueue;

        this.popTime = System.currentTimeMillis();
        this.retryRandom = ThreadLocalRandom.current().nextInt(100);
    }

    public boolean shouldRetry() {
        return retryRandom < brokerConfig.getMessageConfig().getPopRetryProbability();
    }

}
