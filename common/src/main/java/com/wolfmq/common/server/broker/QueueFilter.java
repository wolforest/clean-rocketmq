package com.wolfmq.common.server.broker;

import com.wolfmq.common.domain.messagequeue.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
