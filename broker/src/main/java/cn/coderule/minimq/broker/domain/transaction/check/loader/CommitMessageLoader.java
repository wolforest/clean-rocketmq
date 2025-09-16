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
public class CommitMessageLoader {
    private static final int DEFAULT_PULL_NUM = 32;
    private final MessageService messageService;

    public CommitMessageLoader(TransactionContext transactionContext) {
        this.messageService = transactionContext.getMessageService();
    }

    public DequeueResult load(CheckContext checkContext) {
        return load(checkContext, DEFAULT_PULL_NUM);
    }

    public DequeueResult load(CheckContext context, int num) {
        DequeueResult result = messageService.getMessage(context.getCommitQueue(), num);
        if (!validateCommitResult(context, result)) {
            return result;
        }

        formatCommitResult(context, result);

        return result;
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
            if (offset < context.getPrepareStartOffset()) {
                continue;
            }

            context.linkOffset(message.getQueueOffset(), offset);
            set.add(offset);
        }

        return set;
    }


}
