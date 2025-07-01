package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

/**
 * There are two kinds of messages in transaction:
 * 1. Prepare Message
 *    the message stored before business message,
 *    to start the transaction.
 * 2. Commit Message
 *    the message stored after business message,
 *    to end the transaction.
 *    the transaction may be commited or rollback.
 */
@Data
public class TransactionConfig implements Serializable {
    private long transactionTimeout = 6_000;

    private int maxCommitMessageSize = 4096;
    private int batchCommitInterval = 3_000;

    private int maxBatchCheckSize = 15;
    private int checkInterval = 30_000;
    private int metricsFlushInterval = 3_000;
}
