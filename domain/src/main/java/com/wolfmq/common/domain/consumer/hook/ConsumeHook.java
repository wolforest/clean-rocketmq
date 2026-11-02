package com.wolfmq.common.domain.consumer.hook;

import com.wolfmq.common.domain.consumer.consume.ConsumeContext;

public interface ConsumeHook {
    /**
     * hook name, for hook management
     */
    String hookName();

    /**
     * execute before consume
     * throw @InvalidRequestException to reject consume
     */
    void preConsume(final ConsumeContext context);

    void PostConsume(final ConsumeContext context);
}
