package cn.coderule.minimq.broker.domain.transaction.receipt;

import cn.coderule.minimq.domain.config.business.TransactionConfig;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;

/**
 * transaction receipt manager
 * @rocketmq original name: TransactionDataManager(proxy)
 */
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

    public long getMaxExpireTime() {
        return maxExpireTime.get();
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

    public Receipt poll(String producerGroup, String transactionId) {
        String key = Receipt.buildKey(producerGroup, transactionId);
        long now = System.currentTimeMillis();
        AtomicReference<Receipt> reference = new AtomicReference<>();

        receiptMap.computeIfPresent(key, (k, dataSet) -> {
            Receipt receipt = pollValidReceipt(dataSet, now);
            if (receipt != null) {
                reference.set(receipt);
            }

            // maybe useless ?
            if (dataSet.isEmpty()) {
                return null;
            }

            return dataSet;
        });

        return reference.get();
    }

    public void remove(Receipt receipt) {
        receiptMap.computeIfPresent(receipt.getKey(), (k, dataSet) -> {
            dataSet.remove(receipt);

            if (dataSet.isEmpty()) {
                return null;
            }

            return dataSet;
        });
    }

    public void cleanExpiredReceipts() {
        long now = System.currentTimeMillis();
        Set<String> transactionIdSet = receiptMap.keySet();

        for (String transactionId : transactionIdSet) {
            clean(transactionId, now);
        }
    }

    private void clean(String transactionId, long now) {
        receiptMap.computeIfPresent(transactionId, (k, dataSet) -> {
            clean(dataSet, now);

            if (dataSet.isEmpty()) {
                return null;
            }

            updateMaxExpireTime(dataSet);
            return dataSet;
        });
    }

    private void clean(NavigableSet<Receipt> dataSet, long now) {
        Iterator<Receipt> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            try {
                Receipt receipt = iterator.next();
                if (receipt.getExpireTime() < now) {
                    iterator.remove();
                } else {
                    break;
                }
            } catch (NoSuchElementException ignore) {
                break;
            }
        }
    }

    private void updateMaxExpireTime(NavigableSet<Receipt> dataSet) {
        try {
            Receipt receipt = dataSet.last();
            long expireTime = Math.max(
                receipt.getExpireTime(),
                maxExpireTime.get()
            );

            maxExpireTime.set(expireTime);
         } catch (NoSuchElementException ignore) {
        }
    }

    private Receipt pollValidReceipt(NavigableSet<Receipt> dataSet, long now) {
        Receipt receipt = dataSet.pollLast();
        while (receipt != null && receipt.getExpireTime() < now) {
            receipt = dataSet.pollLast();
        }

        return receipt;
    }

}
