package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.minimq.domain.domain.producer.ProduceContext;
import cn.coderule.minimq.domain.service.broker.produce.ProduceHook;

public class TimerHook implements ProduceHook {
    @Override
    public String hookName() {
        return TimerHook.class.getSimpleName();
    }

    @Override
    public void preProduce(ProduceContext context) {

    }

    @Override
    public void postProduce(ProduceContext context) {

    }
}
