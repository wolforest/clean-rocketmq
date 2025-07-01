package cn.coderule.minimq.domain.service.broker.consume;

import cn.coderule.minimq.domain.domain.consumer.consume.ConsumeContext;

public interface ConsumeHook {
    String hookName();

    void preConsume(final ConsumeContext context);

    void PostConsume(final ConsumeContext context);
}
