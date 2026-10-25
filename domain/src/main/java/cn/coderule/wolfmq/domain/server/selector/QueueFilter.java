package cn.coderule.wolfmq.domain.server.selector;

import cn.coderule.wolfmq.domain.domain.mq.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
