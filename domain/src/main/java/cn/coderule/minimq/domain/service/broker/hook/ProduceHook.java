package cn.coderule.minimq.domain.service.broker.hook;

import cn.coderule.minimq.domain.domain.model.producer.ProduceContext;

public interface ProduceHook {
    String hookName();

    void preProduce(final ProduceContext context);

    void postProduce(final ProduceContext context);
}
