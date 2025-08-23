package cn.coderule.minimq.broker.domain.consumer.renew;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.broker.domain.consumer.consumer.ConsumeHookManager;
import cn.coderule.minimq.domain.config.business.MessageConfig;
import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.consumer.receipt.MessageReceipt;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroup;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandleGroupKey;
import cn.coderule.minimq.domain.domain.consumer.receipt.RenewStrategyPolicy;
import cn.coderule.minimq.domain.domain.consumer.revive.RenewEvent;
import cn.coderule.minimq.domain.service.broker.consume.ReceiptHandler;
import cn.coderule.minimq.domain.service.broker.consume.RetryPolicy;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultReceiptHandler implements ReceiptHandler, Lifecycle {
    private static final RetryPolicy RENEW_POLICY = new RenewStrategyPolicy();

    private BrokerConfig brokerConfig;
    private MessageConfig messageConfig;
    private ConsumeHookManager hookManager;

    private RenewListener renewListener;

    private final ConcurrentMap<ReceiptHandleGroupKey, ReceiptHandleGroup> groupMap
        = new ConcurrentHashMap<>();

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        clearGroup();
    }

    @Override
    public void addReceipt(MessageReceipt messageReceipt) {
        ReceiptHandleGroupKey key = new ReceiptHandleGroupKey(
            messageReceipt.getChannel(),
            messageReceipt.getGroup()
        );

        ReceiptHandleGroup group = groupMap.computeIfAbsent(
            key, k -> new ReceiptHandleGroup(messageConfig)
        );
        group.put(messageReceipt.getMessageId(), messageReceipt);
    }

    @Override
    public MessageReceipt removeReceipt(MessageReceipt messageReceipt) {
        ReceiptHandleGroupKey key = new ReceiptHandleGroupKey(
            messageReceipt.getChannel(),
            messageReceipt.getGroup()
        );

        ReceiptHandleGroup group = groupMap.get(key);
        if (group == null) {
            return null;
        }
        return group.remove(messageReceipt.getMessageId(), messageReceipt.getReceiptHandleStr());
    }

    @Override
    public void removeGroup(ReceiptHandleGroupKey key) {
        if (key == null) {
            return;
        }

        ReceiptHandleGroup group = groupMap.remove(key);
        if (group == null) {
            return;
        }

        group.scan(((msgID, handle, v) -> {
            scanGroup(key, group, msgID, handle);
        }));
    }

    @Override
    public Set<Map.Entry<ReceiptHandleGroupKey, ReceiptHandleGroup>> getEntrySet() {
        return groupMap.entrySet();
    }

    private void scanGroup(
        ReceiptHandleGroupKey key,
        ReceiptHandleGroup group,
        String msgID,
        String handle
    ) {
        try {
            group.computeIfPresent(msgID, handle, receipt -> {
                fireRenewEvent(key, receipt);
                return CompletableFuture.completedFuture(null);
            });
        } catch (Exception e) {
            log.error("clear handle group error, key={}", key, e);
        }
    }

    private void fireRenewEvent(
        ReceiptHandleGroupKey key,
        MessageReceipt receipt
    ) {
        RenewEvent event = RenewEvent.builder()
            .key(key)
            .messageReceipt(receipt)
            .future(new CompletableFuture<>())
            .eventType(RenewEvent.EventType.CLEAR_GROUP)
            .renewTime(messageConfig.getInvisibleTimeOfClear())
            .build();

        renewListener.fire(event);
    }

    private void clearGroup() {
        log.info("start clear receipt handle in {}", this.getClass().getSimpleName());

        for (ReceiptHandleGroupKey key : groupMap.keySet()) {
            removeGroup(key);
        }

        log.info("finish clear receipt handle in {}", this.getClass().getSimpleName());
    }

}
