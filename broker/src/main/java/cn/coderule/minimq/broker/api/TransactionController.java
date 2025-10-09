package cn.coderule.minimq.broker.api;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.broker.domain.transaction.Transaction;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidParameterException;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.meta.topic.TopicValidator;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.transaction.CommitResult;
import java.util.concurrent.CompletableFuture;

public class TransactionController {
    private final Transaction transaction;

    public TransactionController(Transaction transaction) {
        this.transaction = transaction;
    }

    public void subscribe(RequestContext context, String topicName, String groupName) {
        transaction.subscribe(context, topicName, groupName);
    }

    public CompletableFuture<CommitResult> submit(SubmitRequest request) {
        validate(request);

        if (!checkStatus(request)) {
            return CommitResult.failureFuture();
        }

        return transaction.submit(request);
    }

    private void validate(SubmitRequest request) {
        // validate topic
        TopicValidator.validateTopic(request.getTopicName());

        // validate transactionId: not blank
        if (StringUtil.isBlank(request.getTransactionId())) {
            throw new InvalidRequestException(
                InvalidCode.INVALID_TRANSACTION_ID,
                "transactionId is blank"
            );
        }
    }

    private boolean checkStatus(SubmitRequest request) {
        return switch (request.getTransactionFlag()) {
            case MessageSysFlag.COMMIT_MESSAGE, MessageSysFlag.ROLLBACK_MESSAGE -> true;
            default -> false;
        };
    }
}
