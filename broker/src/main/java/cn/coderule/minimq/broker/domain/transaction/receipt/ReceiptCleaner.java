package cn.coderule.minimq.broker.domain.transaction.receipt;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptCleaner extends ServiceThread {
    private final TransactionConfig transactionConfig;
    private final ReceiptRegistry receiptRegistry;

    public ReceiptCleaner(TransactionConfig transactionConfig, ReceiptRegistry receiptRegistry) {
        this.transactionConfig = transactionConfig;
        this.receiptRegistry = receiptRegistry;
    }

    @Override
    public String getServiceName() {
        return ReceiptCleaner.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
