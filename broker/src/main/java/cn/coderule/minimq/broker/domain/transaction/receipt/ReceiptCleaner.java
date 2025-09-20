package cn.coderule.minimq.broker.domain.transaction.receipt;

import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptCleaner extends ServiceThread {
    private final ReceiptRegistry receiptRegistry;

    public ReceiptCleaner(ReceiptRegistry receiptRegistry) {
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
