package cn.coderule.minimq.broker.domain.transaction.receipt;

import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiptRegistry {

    private final AtomicLong maxExpireTime;
    // producerGroup@transactionId -> Set<Receipt>
    private final ConcurrentMap<String, NavigableSet<Receipt>> receiptMap;

    public ReceiptRegistry() {
        long now = System.currentTimeMillis();
        this.maxExpireTime = new AtomicLong(now);

        this.receiptMap = new ConcurrentHashMap<>();
    }


    public void register(Receipt receipt) {

    }

    public void Receipt(String producerGroup, String transactionId) {

    }

    public void remove(Receipt receipt) {

    }

    private String buildKey(String producerGroup, String transactionId) {
        return producerGroup + "@" + transactionId;
    }
}
