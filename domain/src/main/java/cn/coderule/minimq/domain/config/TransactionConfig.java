package cn.coderule.minimq.domain.config;

import java.io.Serializable;
import lombok.Data;

@Data
public class TransactionConfig implements Serializable {
    private long transactionTimeout = 6_000;

    private int maxCommitMessageSize = 4096;
    private int batchCommitInterval = 3_000;

    private int maxBatchCheckSize = 15;
    private int checkInterval = 30_000;
    private int metricsFlushInterval = 3_000;
}
