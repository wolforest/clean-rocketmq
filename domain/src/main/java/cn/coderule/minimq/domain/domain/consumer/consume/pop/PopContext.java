package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.meta.topic.Topic;
import io.netty.channel.Channel;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;

@Data
public class PopContext implements Serializable {
    private final BrokerConfig brokerConfig;
    private final MessageConfig messageConfig;
    private final long popTime;
    private final PopRequest request;
    private final int random;

    private Topic topic;
    private Topic retryTopic;

    private int reviveQueueId;
    private MessageQueue messageQueue = null;

    private Channel channel;

    private StringBuilder startOffsetBuilder;
    private StringBuilder messageOffsetBuilder;
    private StringBuilder orderInfoBuilder;

    public PopContext(BrokerConfig brokerConfig, PopRequest request) {
        this.brokerConfig = brokerConfig;
        this.messageConfig = brokerConfig.getMessageConfig();

        this.request = request;

        this.popTime = System.currentTimeMillis();
        this.random = ThreadLocalRandom.current().nextInt(100);

        this.startOffsetBuilder = new StringBuilder(64);
        this.messageOffsetBuilder = new StringBuilder(64);
        this.orderInfoBuilder = request.isFifo()
            ? new StringBuilder(64)
            : null;
    }

    public boolean shouldRetry() {
        return !request.isFifo()
            && random < messageConfig.getPopRetryProbability();
    }

    public boolean shouldRetryAgain() {
        return !request.isFifo()
            && random >= messageConfig.getPopRetryProbability()
            ;
    }

    public int selectRandomQueue(int queueNum, int count) {
        return (random + count) % queueNum;
    }

    public boolean hasRequestQueueId() {
        return request.getQueueId() >= 0;
    }

    public int getRequestQueueId() {
        return request.getQueueId();
    }

}
