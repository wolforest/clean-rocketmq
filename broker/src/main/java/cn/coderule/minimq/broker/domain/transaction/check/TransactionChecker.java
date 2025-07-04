package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.check.loader.CommitMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.CheckBuffer;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionChecker extends ServiceThread {
    private static final int MAX_CHECK_TIME = 60_000;
    private static final int MAX_RETRY_TIMES = 10;

    private final TransactionContext transactionContext;
    private final TransactionConfig transactionConfig;
    private final QueueTask task;

    private final MessageService messageService;
    private final CheckBuffer checkBuffer;
    private final CommitMessageLoader commitMessageLoader;

    public TransactionChecker(TransactionContext context, QueueTask task) {
        this.task = task;
        this.transactionContext = context;
        this.transactionConfig = context.getBrokerConfig().getTransactionConfig();

        this.messageService = context.getMessageService();
        this.checkBuffer = new CheckBuffer();
        this.commitMessageLoader = new CommitMessageLoader(transactionContext);
    }

    @Override
    public String getServiceName() {
        return TransactionChecker.class.getSimpleName();
    }

    @Override
    public void run() {
        log.info("start transaction checking thread");
        while (!this.isStopped()) {
            long interval = transactionConfig.getCheckInterval();
            this.await(interval);

            check();
        }

        log.info("transaction checking thread end");
    }

    private void check() {
        try {
            String prepareTopic = TransactionUtil.buildPrepareTopic();
            Set<MessageQueue> queueSet = messageService.getMessageQueues(task.getStoreGroup(), prepareTopic);
            if (CollectionUtil.isEmpty(queueSet)) {
                log.warn("no prepare message queue: storeGroup={}, topic={}",
                    task.getStoreGroup(), prepareTopic);
                return;
            }

            log.debug("start check prepare message queue: storeGroup={}, topic={}",
                task.getStoreGroup(), prepareTopic);

            for (MessageQueue mq : queueSet) {
                checkMessageQueue(mq);
            }

        } catch (Throwable e) {
            log.error("transaction check error", e);
        }
    }

    private void checkMessageQueue(MessageQueue prepareQueue) {
        CheckContext checkContext = buildCheckContext(prepareQueue);
        if (!checkContext.isOffsetValid()) {
            log.error("invalid offset for checking: prepareQueue={}, prepareOffset={}, commitOffset={}",
                prepareQueue, checkContext.getPrepareOffset(), checkContext.getCommitOffset());
            return;
        }

        DequeueResult commitResult = loadCommitMessage(checkContext);
        if (commitResult.isEmpty()) {
            updateOffset(checkContext, commitResult);
            return;
        }

        checkContext.initOffset(commitResult.getNextOffset());
        checkCommitResult(checkContext, commitResult);
        updateOffset(checkContext, commitResult);
    }

    private DequeueResult loadCommitMessage(CheckContext checkContext) {
        DequeueResult commitResult = commitMessageLoader.load(checkContext);
        if (validateCommitResult(checkContext, commitResult)) {
            return commitResult;
        }

        formatCommitResult(checkContext, commitResult);

        return commitResult;
    }

    private void formatCommitResult(CheckContext context, DequeueResult result) {

    }

    private boolean validateCommitResult(CheckContext context, DequeueResult result) {
        if (null == result) {
            log.error("illegal result, commitResult can't be null");
            return false;
        }

        if (result.isEmpty()) {
            log.error("no commit message for checking: commitQueue={}, commitOffset={}",
                context.getCommitQueue(), context.getCommitOffset());
            return false;
        }

        if (result.isOffsetIllegal()) {
            log.error("commit message offset illegal: commitQueue={}, commitOffset={}, nextOffset={}",
                context.getCommitQueue(), context.getCommitOffset(), result.getNextOffset());
            return false;
        }

        return true;
    }

    private void checkCommitResult(CheckContext checkContext, DequeueResult commitResult) {
        while (true) {
            if (checkContext.isTimeout(MAX_CHECK_TIME)) {
                log.info("check timeout: prepareQueue={}, maxTime={}ms",
                    checkContext.getPrepareQueue(), MAX_CHECK_TIME);
                break;
            }
        }
    }

    private void updateOffset(CheckContext checkContext, DequeueResult commitResult) {

    }

    private CheckContext buildCheckContext(MessageQueue prepareQueue) {
        MessageQueue commitQueue = checkBuffer.createCommitQueue(prepareQueue);

        CheckContext checkContext = CheckContext.builder()
                .transactionContext(transactionContext)
                .transactionConfig(transactionConfig)
                .prepareQueue(prepareQueue)
                .commitQueue(commitQueue)
                .build();

        initCheckOffset(checkContext);

        return checkContext;
    }

    private void initCheckOffset(CheckContext checkContext) {
        MessageQueue prepareQueue = checkContext.getPrepareQueue();
        long prepareOffset = messageService.getConsumeOffset(prepareQueue);
        checkContext.setPrepareOffset(prepareOffset);

        MessageQueue commitQueue = checkContext.getCommitQueue();
        long commitOffset = messageService.getConsumeOffset(commitQueue);
        checkContext.setCommitOffset(commitOffset);
    }
}
