package cn.coderule.minimq.broker.domain.transaction.check.service;

import cn.coderule.common.util.lang.ThreadUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareMessageChecker {
    private static final int DEFAULT_PULL_NUM = 1;
    private static final int WAIT_WHILE_NO_COMMIT_MESSAGE = 1_000;
    private static final int MAX_INVALID_PREPARE_MESSAGE_NUM = 1;

    private final TransactionConfig transactionConfig;
    private final MessageConfig messageConfig;

    private final OperationMessageLoader operationMessageLoader;

    private final CheckService checkService;
    private final DiscardService discardService;
    private final MessageService messageService;

    public PrepareMessageChecker(TransactionContext context, OperationMessageLoader operationMessageLoader) {
        BrokerConfig brokerConfig = context.getBrokerConfig();
        this.transactionConfig = brokerConfig.getTransactionConfig();
        this.messageConfig = brokerConfig.getMessageConfig();
        this.operationMessageLoader = operationMessageLoader;

        this.checkService = context.getCheckService();
        this.discardService = context.getDiscardService();
        this.messageService = context.getMessageService();
    }

    public boolean check(CheckContext context) {
        DequeueResult result = this.load(context);
        if (result.isEmpty()) {
            return handleEmptyPrepareMessage(context, result);
        }

        if (isOverMaxCheckTimes(result) || isExpired(result)) {
            discardPrepareMessage(context, result);
            return true;
        }

        long now = System.currentTimeMillis();
        if (notTimeout(context, result, now)) {
            return false;
        }

        Long immunityTime = checkImmunityTime(context, result, now);
        if (immunityTime == null) {
            return true;
        }

        if (!needCheck(context, result, now, immunityTime)) {
            loadMoreOperationMessage(context, result);
            return true;
        }

        if (!revivePrepareMessage(result)) {
            return true;
        }

        checkPrepareMessage(context, result);
        return true;
    }

    private DequeueResult load(CheckContext context) {
        return messageService.getMessage(
            context.getPrepareQueue(),
            context.getPrepareOffset(),
            DEFAULT_PULL_NUM
        );
    }

    private boolean handleEmptyPrepareMessage(CheckContext context, DequeueResult result) {
        context.increaseInvalidPrepareMessageCount();

        if (context.getInvalidPrepareMessageCount() > MAX_INVALID_PREPARE_MESSAGE_NUM) {
            return false;
        }

        if (result.overflowOne()) {
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

        context.setPrepareCounter(result.getNextOffset());
        return true;
    }

    private void discardPrepareMessage(CheckContext context, DequeueResult result) {
        discardService.discard(result.getMessage());
        context.increasePrepareCounter();
    }

    private boolean isExpired(DequeueResult result) {
        MessageBO message = result.getMessage();
        long timeout = messageConfig.getFileReservedTime();
        long elapsed = System.currentTimeMillis() - message.getBornTimestamp();

        if (elapsed <= timeout) {
            return false;
        }

        log.info("Prepare message timeout, message={}, bornTime:{}, timeout={}",
            message, message.getBornTimestamp(), timeout);
        return true;
    }

    private boolean isOverMaxCheckTimes(DequeueResult result) {
        long maxTimes = transactionConfig.getMaxCheckTimes();
        MessageBO message = result.getMessage();
        int checkTimes = message.getIntProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES);

        if (checkTimes >= maxTimes) {
            return true;
        }

        checkTimes++;
        String checkTimesStr = String.valueOf(checkTimes);
        message.putSystemProperty(MessageConst.PROPERTY_TRANSACTION_CHECK_TIMES, checkTimesStr);
        return false;
    }

    private boolean notTimeout(CheckContext context, DequeueResult result, long now) {
        if (isStoreAfterCheck(context, result)) {
            return true;
        }

        return isTransactionTimeout(result, now);
    }

    private boolean isStoreAfterCheck(CheckContext context, DequeueResult result) {
        MessageBO message = result.getMessage();

        long storeAt = message.getStoreTimestamp();
        if (storeAt < context.getStartTime()) {
            return false;
        }

        log.debug("Fresh prepare message, check it later. offset={}, msgStoreTime={}",
            context.getPrepareOffset(), DateUtil.asLocalDateTime(message.getBornTimestamp()));
        return true;
    }

    private boolean isTransactionTimeout(DequeueResult result, long now) {
        MessageBO message = result.getMessage();
        long messageAge = now - message.getBornTimestamp();
        long checkTime = message.getTransactionCheckTime();

        if (checkTime >= 0) {
            log.debug("transaction check time is null: message={}", message);
            return false;
        }

        if (messageAge < 0) {
            log.debug("message was just born: age={}, message={}", messageAge, message);
            return false;
        }

        return messageAge < transactionConfig.getTransactionTimeout();
    }

    private Long checkImmunityTime(CheckContext context, DequeueResult result, long now) {
        MessageBO message = result.getMessage();
        long messageAge = now - message.getBornTimestamp();
        long checkTime = message.getTransactionCheckTime();

        if (checkTime < 0) {
            checkTime = transactionConfig.getTransactionTimeout();
        } else {
            checkTime *= 1000;
        }

        if (messageAge >= checkTime) {
            return checkTime;
        }

        if (!checkPrepareQueueOffset(context, result)) {
            return checkTime;
        }

        context.increasePrepareCounter();
        return null;
    }

    private boolean checkPrepareQueueOffset(CheckContext context, DequeueResult result) {
        MessageBO message = result.getMessage();
        String offsetString = message.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        if (null == offsetString) {
            return revivePrepareMessage(message).isSuccess();
        }

        long prepareOffset = StringUtil.getLong(offsetString, -1);
        if (prepareOffset < 0) {
            return false;
        }

        if (!context.containsPrepareOffset(prepareOffset)) {
            return revivePrepareMessage(message).isSuccess();
        }

        context.removePrepareOffset(prepareOffset);
        return true;
    }

    private EnqueueResult revivePrepareMessage(MessageBO messageBO) {
        try {
            MessageBO prepareMessage = recreatePrepareMessage(messageBO);
            return messageService.enqueueMessage(prepareMessage);
        } catch (Exception e) {
            log.warn("revive prepare message error", e);
        }

        return EnqueueResult.failure();
    }

    private MessageBO recreatePrepareMessage(MessageBO messageBO) {
        MessageBO newMsg = MessageBO.builder()
            .topic(messageBO.getTopic())
            .body(messageBO.getBody())
            .queueId(messageBO.getQueueId())
            .messageId(messageBO.getMessageId())
            .sysFlag(messageBO.getSysFlag())
            .properties(messageBO.getProperties())
            .bornTimestamp(messageBO.getBornTimestamp())
            .bornHost(messageBO.getBornHost())
            .storeHost(messageBO.getStoreHost())
            .build();

        newMsg.setTags(messageBO.getTags());
        setQueueOffset(messageBO, newMsg);
        newMsg.setWaitStore(false);
        return newMsg;
    }

    private void setQueueOffset(MessageBO messageBO, MessageBO newMsg) {
        String offsetStr = messageBO.getProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        if (StringUtil.notBlank(offsetStr)) {
            newMsg.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, offsetStr);
        } else {
            newMsg.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET, String.valueOf(messageBO.getQueueOffset()));
        }
    }

    private boolean needCheck(CheckContext context, DequeueResult result, long now, long immunityTime) {
        MessageBO message = result.getFirstMessage();
        long firstMessageAge = now - message.getBornTimestamp();
        if (firstMessageAge <= -1) {
            return true;
        }

        if (result.isEmpty() && firstMessageAge > immunityTime) {
            return true;
        }

        if (result.isEmpty()) {
            return false;
        }

        MessageBO lastMessage = result.getLastMessage();
        long lastMessageAge = now - lastMessage.getBornTimestamp();
        return lastMessageAge > transactionConfig.getTransactionTimeout();
    }

    private boolean revivePrepareMessage(DequeueResult dequeueResult) {
        MessageBO messageBO = dequeueResult.getMessage();

        EnqueueResult enqueueResult = revivePrepareMessage(dequeueResult.getMessage());
        if (!enqueueResult.isSuccess()) {
            log.error("revive prepare message error: message={}, result={}", dequeueResult.getMessage(), enqueueResult);
            return false;
        }

        messageBO.setQueueOffset(enqueueResult.getQueueOffset());
        messageBO.setCommitOffset(enqueueResult.getCommitOffset());
        messageBO.setMessageId(enqueueResult.getMessageId());
        return true;
    }

    private void checkPrepareMessage(CheckContext context, DequeueResult result) {
        context.increaseMessageCheckCount();
        checkService.check(result.getMessage());
    }

    private void resetNextOperationOffset(CheckContext context, DequeueResult lastResult) {
        long tmpOffset = lastResult.hasNextOffset()
            ? lastResult.getNextOffset()
            : context.getNextOperationOffset();
        context.setNextOperationOffset(tmpOffset);
    }

    private void loadMoreOperationMessage(CheckContext context, DequeueResult lastResult) {
        resetNextOperationOffset(context, lastResult);
        DequeueResult result = operationMessageLoader.load(context);
        context.setNextOperationOffset(result.getNextOffset());

        if (shouldSleep(result)) {
            ThreadUtil.sleep(WAIT_WHILE_NO_COMMIT_MESSAGE);
        }
    }

    private boolean shouldSleep(DequeueResult result) {
        return result == null
            || result.overflowOne()
            || result.isEmpty()
            || result.isOffsetIllegal();
    }

}
