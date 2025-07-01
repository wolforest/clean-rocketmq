package cn.coderule.minimq.domain.service.broker.produce;

import cn.coderule.minimq.domain.domain.producer.ProduceContext;

public interface ProduceHook {
    String hookName();

    void preProduce(final ProduceContext context);

    void postProduce(final ProduceContext context);
}
