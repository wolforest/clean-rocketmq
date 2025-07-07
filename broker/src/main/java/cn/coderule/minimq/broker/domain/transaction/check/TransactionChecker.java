package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.check.loader.CommitMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.check.loader.PrepareMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
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
    private final PrepareMessageLoader prepareMessageLoader;

    public TransactionChecker(TransactionContext context, QueueTask task) {
        this.task = task;
        this.transactionContext = context;
        this.transactionConfig = context.getBrokerConfig().getTransactionConfig();

        this.messageService = context.getMessageService();
        this.checkBuffer = new CheckBuffer();
        this.commitMessageLoader = new CommitMessageLoader(transactionContext);
        this.prepareMessageLoader = new PrepareMessageLoader(transactionContext, commitMessageLoader);
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
        CheckContext context = buildCheckContext(prepareQueue);
        if (!context.isOffsetValid()) {
            return;
        }

        DequeueResult commitResult = commitMessageLoader.load(context);
        if (commitResult.isEmpty()) {
            updateOffset(context, commitResult);
            return;
        }

        context.initOffset(commitResult.getNextOffset());
        checkMessage(context);
        updateOffset(context, commitResult);
    }

    private void removePrepareOffset(CheckContext context) {
        log.debug("prepare offset has been committed/rollback: {}", context.getPrepareOffset());
        context.removePrepareOffset(context.getPrepareOffset());
    }

    private void checkMessage(CheckContext context) {
        while (true) {
            if (context.isTimeout(MAX_CHECK_TIME)) {
                log.info("check timeout: prepareQueue={}, maxTime={}ms",
                    context.getPrepareQueue(), MAX_CHECK_TIME);
                break;
            }

            if (context.containsPrepareOffset(context.getPrepareOffset())) {
                removePrepareOffset(context);
                context.increasePrepareOffset();
                continue;
            }

            if (!prepareMessageLoader.loadAndCheck(context)) {
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
        checkContext.setPrepareStartOffset(prepareOffset);

        MessageQueue commitQueue = checkContext.getCommitQueue();
        long commitOffset = messageService.getConsumeOffset(commitQueue);
        checkContext.setCommitOffset(commitOffset);
    }
}
