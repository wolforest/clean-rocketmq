package cn.coderule.minimq.domain.service.broker.produce;

import cn.coderule.minimq.domain.domain.producer.ProduceContext;

/**
 * message produce hook.
 * possible hooks:
 *  - check service ready hook
 *  - check inner batch hook
 *  - timer hook, to change timer message topic
 */
public interface ProduceHook {
    /**
     * hook name, for hook management
     */
    String hookName();

    /**
     * execute before produce
     * throw @InvalidRequestException to reject produce
     */
    void preProduce(final ProduceContext context);

    void postProduce(final ProduceContext context);
}
