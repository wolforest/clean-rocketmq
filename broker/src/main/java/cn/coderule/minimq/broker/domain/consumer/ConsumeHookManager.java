package cn.coderule.minimq.broker.domain.consumer;

import cn.coderule.minimq.domain.domain.model.consumer.ConsumeContext;
import cn.coderule.minimq.domain.service.broker.hook.ConsumeHook;

public class ConsumeHookManager implements ConsumeHook {
    @Override
    public String hookName() {
        return ConsumeHookManager.class.getSimpleName();
    }

    @Override
    public void preConsume(ConsumeContext context) {

    }

    @Override
    public void PostConsume(ConsumeContext context) {

    }
}
