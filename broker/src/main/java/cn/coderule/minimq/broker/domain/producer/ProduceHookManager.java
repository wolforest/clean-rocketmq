package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.domain.service.broker.hook.ProduceContext;
import cn.coderule.minimq.domain.service.broker.hook.ProduceHook;

public class ProduceHookManager implements ProduceHook {
    @Override
    public String hookName() {
        return ProduceHookManager.class.getSimpleName();
    }

    @Override
    public void preProduce(ProduceContext context) {

    }

    @Override
    public void postProduce(ProduceContext context) {

    }
}
