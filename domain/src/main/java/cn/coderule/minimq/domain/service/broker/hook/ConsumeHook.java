package cn.coderule.minimq.domain.service.broker.hook;

import cn.coderule.minimq.domain.domain.consumer.ConsumeContext;

public interface ConsumeHook {
    String hookName();

    void preConsume(final ConsumeContext context);

    void PostConsume(final ConsumeContext context);
}
