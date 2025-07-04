package cn.coderule.minimq.broker.domain.transaction.check.loader;

import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitMessageLoader {
    private static final int DEFAULT_PULL_NUM = 32;
    private MessageService messageService;
    private TransactionContext transactionContext;

    public CommitMessageLoader(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
        this.messageService = transactionContext.getMessageService();
    }

    public DequeueResult load(CheckContext checkContext) {
        return load(checkContext, DEFAULT_PULL_NUM);
    }

    public DequeueResult load(CheckContext checkContext, int num) {
        return DequeueResult.notFound();
    }
}
