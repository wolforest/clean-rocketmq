package cn.coderule.minimq.broker.domain.transaction.check.loader;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.transaction.check.context.CheckContext;
import cn.coderule.minimq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.minimq.broker.domain.transaction.service.MessageService;
import cn.coderule.minimq.domain.domain.store.domain.mq.DequeueResult;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OperationMessageLoader {
    private static final int DEFAULT_PULL_NUM = 32;
    private final MessageService messageService;

    public OperationMessageLoader(TransactionContext transactionContext) {
        this.messageService = transactionContext.getMessageService();
    }

    public DequeueResult load(CheckContext checkContext) {
        return load(checkContext, DEFAULT_PULL_NUM);
    }

    public DequeueResult load(CheckContext context, int num) {
        DequeueResult result = messageService.getMessage(context.getOperationQueue(), context.getOperationOffset(), num);
        if (!validateResult(context, result)) {
            return result;
        }

        formatOperationResult(context, result);

        return result;
    }

    private boolean validateResult(CheckContext context, DequeueResult result) {
        if (null == result) {
            log.error("illegal result, operationResult can't be null");
            return false;
        }

        if (result.isEmpty()) {
            log.error("no operation message for checking: operationQueue={}, operationOffset={}",
                context.getOperationQueue(), context.getOperationOffset());
            return false;
        }

        if (result.isOffsetIllegal()) {
            log.error("operation message offset illegal: operationQueue={}, operationOffset={}, nextOffset={}",
                context.getOperationQueue(), context.getOperationOffset(), result.getNextOffset());
            return false;
        }

        return true;
    }

    private void handleEmptyCommitMessage(CheckContext context, MessageBO message) {
        log.error("body of operationMessage is null, queueId={}, offset={}",
            message.getQueueId(), message.getQueueOffset());

        context.addCommittedOffset(message.getQueueOffset());
    }

    private void formatOperationResult(CheckContext context, DequeueResult result) {
        for (MessageBO message : result.getMessageList()) {
            if (null == message.getBody()) {
                handleEmptyCommitMessage(context, message);
                continue;
            }

            Set<Long> prepareOffsetSet = getOperationOffset(context, message);
            if (prepareOffsetSet.isEmpty()) {
                context.addCommittedOffset(message.getQueueOffset());
                continue;
            }

            context.putOffsetMap(message.getQueueOffset(), prepareOffsetSet);
        }
    }

    private boolean validateBody(MessageBO message, String body) {
        log.debug("parse operationMessage: topic={}, tags={}, commitOffset={}, prepareOffsets={}",
            message.getTopic(), message.getTags(), message.getQueueOffset(), body);

        if (StringUtil.isBlank(body)) {
            log.error("operationMessage body is null, message={}", message);
            return false;
        }

        if (!TransactionUtil.REMOVE_TAG.equals(message.getTags())) {
            log.error("operation message tag is not remove tag, message={}", message);
            return false;
        }

        return true;

    }

    private Set<Long> getOperationOffset(CheckContext context, MessageBO message) {
        String body = message.getBodyString();
        Set<Long> set = new HashSet<>();
        if (!validateBody(message, body)) {
            return set;
        }

        String[] arr = body.split(TransactionUtil.OFFSET_SEPARATOR);
        for (String offsetString : arr) {
            Long offset = StringUtil.getLong(offsetString, -1);
            if (offset < context.getStartPrepareOffset()) {
                continue;
            }

            context.linkOffset(message.getQueueOffset(), offset);
            set.add(offset);
        }

        return set;
    }


}
