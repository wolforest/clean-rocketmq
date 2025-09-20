package cn.coderule.minimq.broker.domain.transaction.receipt;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptRegistry {
    private final TransactionConfig transactionConfig;
    private final AtomicLong maxExpireTime;
    // producerGroup@transactionId -> Set<Receipt>
    private final ConcurrentMap<String, NavigableSet<Receipt>> receiptMap;

    public ReceiptRegistry(TransactionConfig transactionConfig) {
        this.transactionConfig = transactionConfig;

        long now = System.currentTimeMillis();
        this.maxExpireTime = new AtomicLong(now);

        this.receiptMap = new ConcurrentHashMap<>();
    }


    public void register(Receipt receipt) {
        receiptMap.compute(receipt.getKey(), (key, dataSet) -> {
            if (dataSet == null) {
                dataSet = new ConcurrentSkipListSet<>();
            }

            dataSet.add(receipt);

            if (dataSet.size() > transactionConfig.getMaxReceiptNum()) {
                dataSet.pollFirst();
            }

            return dataSet;
        });
    }

    public void Receipt(String producerGroup, String transactionId) {

    }

    public void remove(Receipt receipt) {

    }

}
