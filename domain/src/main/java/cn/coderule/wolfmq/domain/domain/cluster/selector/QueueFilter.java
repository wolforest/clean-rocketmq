package cn.coderule.wolfmq.domain.domain.cluster.selector;

import cn.coderule.wolfmq.domain.domain.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
