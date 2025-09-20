package cn.coderule.minimq.broker.domain.transaction.receipt;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import cn.coderule.common.util.lang.ThreadUtil;
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
        log.info("{} service started", this.getServiceName());

        while (!this.isStopped()) {
            await(transactionConfig.getReceiptScanInterval());
            clean();
        }

        log.info("{} service stopped", this.getServiceName());
    }

    private void clean() {
        receiptRegistry.cleanExpiredReceipts();

        long now = System.currentTimeMillis();
        long waitTime = Math.max(0, receiptRegistry.getMaxExpireTime() - now);
        waitTime = Math.min(waitTime, transactionConfig.getReceiptCleanInterval());

        if (waitTime <= 0) {
            return;
        }

        ThreadUtil.sleep(waitTime);
    }

}
