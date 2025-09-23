package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.check.loader.OperationMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.check.loader.PrepareMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
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
    private final OperationMessageLoader operationMessageLoader;
    private final PrepareMessageLoader prepareMessageLoader;

    public TransactionChecker(TransactionContext context, QueueTask task) {
        this.task = task;
        this.transactionContext = context;
        this.transactionConfig = context.getBrokerConfig().getTransactionConfig();

        this.messageService = context.getMessageService();
        this.checkBuffer = new CheckBuffer();
        this.operationMessageLoader = new OperationMessageLoader(transactionContext);
        this.prepareMessageLoader = new PrepareMessageLoader(transactionContext, operationMessageLoader);
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
                checkPreparedQueue(mq);
            }

        } catch (Throwable e) {
            log.error("transaction check error", e);
        }
    }

    private void checkPreparedQueue(MessageQueue prepareQueue) {
        CheckContext context = buildCheckContext(prepareQueue);
        if (!context.isOffsetValid()) {
            return;
        }

        DequeueResult result = operationMessageLoader.load(context);
        if (result == null) {
            log.error("operation message load result is null: CheckContext={}", context);
            return;
        }

        context.initOffset(result.getNextOffset());
        checkMessage(context);
        updateOffset(context, result);
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
            } else if (!prepareMessageLoader.loadAndCheck(context)) {
                break;
            }

            context.increasePrepareCounter();
        }
    }

    private void updateOffset(CheckContext checkContext, DequeueResult commitResult) {

    }

    private void removePrepareOffset(CheckContext context) {
        log.debug("prepare offset has been committed/rollback: {}", context.getPrepareOffset());
        context.removePrepareOffset(context.getPrepareOffset());
    }

    private CheckContext buildCheckContext(MessageQueue prepareQueue) {
        MessageQueue operationQueue = checkBuffer.mapQueue(prepareQueue);

        CheckContext checkContext = CheckContext.builder()
                .transactionContext(transactionContext)
                .transactionConfig(transactionConfig)
                .prepareQueue(prepareQueue)
                .operationQueue(operationQueue)
                .build();

        initCheckOffset(checkContext);
        return checkContext;
    }

    private void initCheckOffset(CheckContext context) {
        long prepareOffset = messageService.getConsumeOffset(context.getPrepareQueue());
        context.setPrepareOffset(prepareOffset);

        long operationOffset = messageService.getConsumeOffset(context.getOperationQueue());
        context.setOperationOffset(operationOffset);
    }
}
