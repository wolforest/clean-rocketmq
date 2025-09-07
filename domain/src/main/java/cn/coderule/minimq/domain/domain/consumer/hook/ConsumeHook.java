package cn.coderule.minimq.domain.domain.consumer.hook;

import cn.coderule.minimq.domain.domain.consumer.consume.ConsumeContext;

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
