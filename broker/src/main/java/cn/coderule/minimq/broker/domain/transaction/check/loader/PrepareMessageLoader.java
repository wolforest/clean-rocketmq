package cn.coderule.minimq.broker.domain.transaction.check.loader;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareMessageLoader {
    private static final int DEFAULT_PULL_NUM = 1;
    private static final int WAIT_WHILE_NO_COMMIT_MESSAGE = 1_000;
    private static final int MAX_INVALID_PREPARE_MESSAGE_NUM = 1;
    private final MessageService messageService;
    private final TransactionConfig transactionConfig;
    private final CommitMessageLoader commitMessageLoader;
    private final TransactionContext transactionContext;

    public PrepareMessageLoader(TransactionContext transactionContext, CommitMessageLoader commitMessageLoader) {
        this.transactionContext = transactionContext;
        this.transactionConfig = transactionContext.getBrokerConfig().getTransactionConfig();
        this.commitMessageLoader = commitMessageLoader;
        this.messageService = transactionContext.getMessageService();
    }

    public DequeueResult load(CheckContext checkContext) {
        return load(checkContext, DEFAULT_PULL_NUM);
    }

    public DequeueResult load(CheckContext checkContext, int num) {
        return DequeueResult.notFound();
    }

    public boolean loadAndCheck(CheckContext context) {
        DequeueResult result = this.load(context);
        if (result.isEmpty()) {
            return handleEmptyPrepareMessage(context, result);
        }

        if (isOverMaxCheckTimes(context, result)) {
            discardPrepareMessage(context, result);
            return true;
        }

        long now = System.currentTimeMillis();
        if (isInTime(context, result, now)) {
            return false;
        }

        Long immunityTime = getImmunityTime(context, result, now);
        if (immunityTime == null) {
            context.increasePrepareOffset();
            return false;
        }

        if (!needCheck(context, result, now, immunityTime)) {
            loadMoreCommitMessage(context);
            return true;
        }

        renewPrepareMessage(context, result);
        return true;
    }

    private boolean handleEmptyPrepareMessage(CheckContext context, DequeueResult result) {
        context.increaseInvalidPrepareMessageCount();

        if (context.getInvalidPrepareMessageCount() > MAX_INVALID_PREPARE_MESSAGE_NUM) {
            return false;
        }

        if (result.noNewMessage()) {
            log.debug("No new prepare message, context={}", context);
            return false;
        }

        log.info("illegal prepare message offset,"
                + " offset={}, queue={}, illegalCount={}, result={}",
            context.getPrepareOffset(),
            context.getPrepareQueue(),
            context.getInvalidPrepareMessageCount(),
            result
        );
        context.setPrepareOffset(result.getNextOffset());
        return true;
    }

    private void discardPrepareMessage(CheckContext context, DequeueResult result) {

    }

    private boolean isOverMaxCheckTimes(CheckContext context, DequeueResult result) {
        return false;
    }

    private boolean isBornBeforeCheck(CheckContext context, DequeueResult result) {
        MessageBO message = result.getMessage();

        long bornAt = message.getBornTimestamp();
        if (bornAt < context.getStartTime()) {
            return false;
        }

        log.debug("Fresh prepare message, check it later. offset={}, msgStoreTime={}",
            context.getPrepareOffset(), DateUtil.asLocalDateTime(message.getBornTimestamp()));

        return true;
    }

    private boolean isTransactionTimeout(CheckContext context, DequeueResult result, long now) {
        return false;
    }

    private boolean isInTime(CheckContext context, DequeueResult result, long now) {
        if (isBornBeforeCheck(context, result)) {
            return false;
        }

        return isTransactionTimeout(context, result, now);
    }

    private Long getImmunityTime(CheckContext context, DequeueResult result, long now) {
        return null;
    }

    private boolean needCheck(CheckContext context, DequeueResult result, long now, long immunityTime) {
        if (now <= -1) {
            return true;
        }

        if (result.isEmpty() && now > immunityTime) {
            return true;
        }

        if (result.isEmpty()) {
            return false;
        }

        MessageBO message = result.getLastMessage();
        return message.getBornTimestamp() - context.getStartTime() > transactionConfig.getTransactionTimeout();
    }

    private void renewPrepareMessage(CheckContext context, DequeueResult result) {
        increaseRenewCount(context);
    }

    private void increaseRenewCount(CheckContext context) {
    }

    private void loadMoreCommitMessage(CheckContext context) {
        DequeueResult result = commitMessageLoader.load(context);

        if (result == null
            || result.noNewMessage()
            || result.isEmpty()
            || result.isOffsetIllegal()
        ) {
            ThreadUtil.sleep(WAIT_WHILE_NO_COMMIT_MESSAGE);
            return;
        }

        context.setCommitNextOffset(result.getNextOffset());
    }

}
