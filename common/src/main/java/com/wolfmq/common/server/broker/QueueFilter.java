package com.wolfmq.common.server.broker;

import com.wolfmq.common.domain.mq.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
