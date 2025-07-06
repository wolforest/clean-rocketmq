package cn.coderule.minimq.broker.domain.transaction.check;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.check.loader.CommitMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.check.loader.PrepareMessageLoader;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.TransactionConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.cluster.task.QueueTask;
import cn.coderule.minimq.domain.domain.consumer.consume.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.CheckBuffer;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionChecker extends ServiceThread {
    private static final int MAX_CHECK_TIME = 60_000;
    private static final int MAX_INVALID_PREPARE_MESSAGE_NUM = 1;
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
        this.prepareMessageLoader = new PrepareMessageLoader(transactionContext);
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

        DequeueResult commitResult = loadCommitMessage(context);
        if (commitResult.isEmpty()) {
            updateOffset(context, commitResult);
            return;
        }

        context.initOffset(commitResult.getNextOffset());
        checkPrepareMessage(context);
        updateOffset(context, commitResult);
    }

    private DequeueResult loadCommitMessage(CheckContext checkContext) {
        DequeueResult commitResult = commitMessageLoader.load(checkContext);
        if (validateCommitResult(checkContext, commitResult)) {
            return commitResult;
        }

        formatCommitResult(checkContext, commitResult);

        return commitResult;
    }

    private void handleEmptyCommitMessage(CheckContext context, MessageBO message) {
        log.error("body of commitMessage is null, queueId={}, offset={}",
            message.getQueueId(), message.getQueueOffset());

        context.addCommittedOffset(message.getQueueOffset());
    }

    private void formatCommitResult(CheckContext context, DequeueResult result) {
        for (MessageBO message : result.getMessageList()) {
            if (null == message.getBody()) {
                handleEmptyCommitMessage(context, message);
                continue;
            }

            Set<Long> prepareOffsetSet = getCommitOffset(context, message);
            if (prepareOffsetSet.isEmpty()) {
                context.addCommittedOffset(message.getQueueOffset());
                continue;
            }

            context.putOffsetMap(message.getQueueOffset(), prepareOffsetSet);
        }
    }

    private boolean validateCommitBody(MessageBO message, String body) {
        log.debug("parse commitMessage: topic={}, tags={}, commitOffset={}, prepareOffsets={}",
            message.getTopic(), message.getTags(), message.getQueueOffset(), body);

        if (StringUtil.isBlank(body)) {
            log.error("commitMessage body is null, message={}", message);
            return false;
        }

        if (!TransactionUtil.REMOVE_TAG.equals(message.getTags())) {
            log.error("commit message tag is not remove tag, message={}", message);
            return false;
        }

        return true;

    }

    private Set<Long> getCommitOffset(CheckContext context, MessageBO message) {
        String body = message.getBodyString();
        Set<Long> set = new HashSet<>();
        if (!validateCommitBody(message, body)) {
            return set;
        }

        String[] arr = body.split(TransactionUtil.OFFSET_SEPARATOR);
        for (String offsetString : arr) {
            Long offset = StringUtil.getLong(offsetString, -1);
            if (offset < context.getPrepareOffset()) {
                continue;
            }

            context.linkOffset(message.getQueueOffset(), offset);
            set.add(offset);
        }

        return set;
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

    private void removePrepareOffset(CheckContext context) {
        log.debug("prepare offset has been committed/rollback: {}", context.getPrepareCounter());
        context.removePrepareOffset(context.getPrepareCounter());
    }

    private boolean handleEmptyPrepareMessage(CheckContext context, DequeueResult result) {
        context.increaseInvalidPrepareMessageCount();

        if (context.getInvalidPrepareMessageCount() > MAX_INVALID_PREPARE_MESSAGE_NUM) {
            return false;
        }

        if (!result.hasNewMessage()) {
            log.debug("No new prepare message, context={}", context);
            return false;
        }

        log.info("illegal prepare message offset,"
                + " offset={}, queue={}, illegalCount={}, result={}",
            context.getPrepareCounter(),
            context.getPrepareQueue(),
            context.getInvalidPrepareMessageCount(),
            result
        );
        context.setPrepareCounter(result.getNextOffset());
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
            context.getPrepareCounter(), DateUtil.asLocalDateTime(message.getBornTimestamp()));

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

    private boolean needCheck(CheckContext context, long now, long immunityTime) {
        return false;
    }

    private void loadMoreCommitMessage(CheckContext context) {
    }

    private boolean renewPrepareMessage(CheckContext context, DequeueResult result) {
        return false;
    }

    private void increaseRenewCount(CheckContext context) {
    }

    private boolean loadAndCheck(CheckContext context) {
        DequeueResult result = prepareMessageLoader.load(context);
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
            context.increasePrepareCounter();
            return false;
        }

        if (!needCheck(context, now, immunityTime)) {
            loadMoreCommitMessage(context);
            return true;
        }

        if (!renewPrepareMessage(context, result)) {
            return true;
        }

        increaseRenewCount(context);
        return true;
    }

    private void checkPrepareMessage(CheckContext context) {
        while (true) {
            if (context.isTimeout(MAX_CHECK_TIME)) {
                log.info("check timeout: prepareQueue={}, maxTime={}ms",
                    context.getPrepareQueue(), MAX_CHECK_TIME);
                break;
            }

            if (context.containsPrepareOffset(context.getPrepareCounter())) {
                removePrepareOffset(context);
            } else if (!loadAndCheck(context)) {
                break;
            }

            context.increasePrepareCounter();
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
