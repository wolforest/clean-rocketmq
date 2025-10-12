package cn.coderule.minimq.domain.config.business;

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
    private int maxReceiptNum = 15;

    private long receiptScanInterval = 10_000;
    private long receiptExpireTime = 60_000;
    private long receiptCleanInterval = 30_000;

    private int maxCommitMessageLength = 4096;
    private int batchCommitInterval = 3_000;

    private long checkTimeout = 6_000;
    private int checkInterval = 30_000;
    private int maxCheckTimes = 15;

    private int checkThreadNum = 2;
    private int maxCheckThreadNum = 5;
    private int keepAliveTime = 100_000;
    private int checkQueueCapacity = 2_000;

    private int metricsFlushInterval = 3_000;
}
