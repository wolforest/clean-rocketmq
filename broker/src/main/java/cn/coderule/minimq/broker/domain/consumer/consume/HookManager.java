package cn.coderule.minimq.broker.domain.consumer.consume;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.model.consumer.ConsumeContext;
import cn.coderule.minimq.domain.service.broker.hook.ConsumeHook;
import java.util.ArrayList;
import java.util.List;

public class HookManager implements ConsumeHook {
    private final List<ConsumeHook> hooks = new ArrayList<>();

    public void registerHook(ConsumeHook hook) {
        hooks.add(hook);
    }

    @Override
    public String hookName() {
        return HookManager.class.getSimpleName();
    }

    @Override
    public void preConsume(ConsumeContext context) {
        if (CollectionUtil.isEmpty(hooks)) {
            return;
        }

        for (ConsumeHook hook : hooks) {
            hook.preConsume(context);
        }
    }

    @Override
    public void PostConsume(ConsumeContext context) {
        if (CollectionUtil.isEmpty(hooks)) {
            return;
        }

        for (ConsumeHook hook : hooks) {
            hook.PostConsume(context);
        }
    }
}
