package com.wolfmq.domain.server.selector;

import com.wolfmq.domain.domain.mq.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
