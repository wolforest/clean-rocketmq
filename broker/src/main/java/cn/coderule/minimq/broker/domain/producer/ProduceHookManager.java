package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.domain.producer.ProduceContext;
import cn.coderule.minimq.domain.service.broker.consume.ConsumeHook;
import cn.coderule.minimq.domain.service.broker.produce.ProduceHook;
import java.util.ArrayList;
import java.util.List;

public class ProduceHookManager implements ProduceHook {
    private final List<ProduceHook> hooks = new ArrayList<>();
    @Override
    public String hookName() {
        return ProduceHookManager.class.getSimpleName();
    }

    public void registerHook(ProduceHook hook) {
        hooks.add(hook);
    }

    @Override
    public void preProduce(ProduceContext context) {
        if (CollectionUtil.isEmpty(hooks)) {
            return;
        }

        for (ProduceHook hook : hooks) {
            hook.preProduce(context);
        }
    }

    @Override
    public void postProduce(ProduceContext context) {
        if (CollectionUtil.isEmpty(hooks)) {
            return;
        }

        for (ProduceHook hook : hooks) {
            hook.postProduce(context);
        }
    }

    public List<String> getHookNameList() {
        List<String> hookNames = new ArrayList<>();
        for (ProduceHook hook : hooks) {
            hookNames.add(hook.hookName());
        }
        return hookNames;
    }

}
