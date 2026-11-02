package com.wolfmq.common.server.selector;

import com.wolfmq.common.domain.mq.MessageQueue;

public interface QueueFilter {
    boolean filter(MessageQueue mq);
}
