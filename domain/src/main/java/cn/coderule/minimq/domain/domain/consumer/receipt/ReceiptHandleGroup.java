
package cn.coderule.minimq.domain.domain.model.consumer.receipt;

import cn.coderule.minimq.domain.config.message.MessageConfig;
import cn.coderule.minimq.domain.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.domain.core.exception.BrokerException;
import com.google.common.base.MoreObjects;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ReceiptHandleGroup {

    // The messages having the same messageId will be deduplicated based on the parameters of broker, queueId, and offset
    protected final Map<String /* msgID */, Map<HandleKey, HandleData>> receiptHandleMap = new ConcurrentHashMap<>();
    protected final MessageConfig messageConfig;

    public ReceiptHandleGroup(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    public void put(String msgID, MessageReceipt value) {
        long timeout = this.messageConfig.getLockTimeoutMsInHandleGroup();

        Map<HandleKey, HandleData> handleMap = this.receiptHandleMap.computeIfAbsent(msgID, msgIDKey -> new ConcurrentHashMap<>());
        handleMap.compute(new HandleKey(value.getOriginalReceiptHandle()), (handleKey, handleData) -> {
            if (handleData == null || handleData.isNeedRemove()) {
                return new HandleData(value);
            }
            if (!handleData.lock(timeout)) {
                throw new BrokerException(BrokerExceptionCode.INTERNAL_SERVER_ERROR, "try to put handle failed");
            }
            try {
                if (handleData.isNeedRemove()) {
                    return new HandleData(value);
                }
                handleData.setMessageReceipt(value);
            } finally {
                handleData.unlock();
            }
            return handleData;
        });
    }

    public boolean isEmpty() {
        return this.receiptHandleMap.isEmpty();
    }

    public MessageReceipt get(String msgID, String handle) {
        Map<HandleKey, HandleData> handleMap = this.receiptHandleMap.get(msgID);
        if (handleMap == null) {
            return null;
        }
        long timeout = this.messageConfig.getLockTimeoutMsInHandleGroup();
        AtomicReference<MessageReceipt> res = new AtomicReference<>();
        handleMap.computeIfPresent(new HandleKey(handle), (handleKey, handleData) -> {
            if (!handleData.lock(timeout)) {
                throw new BrokerException(BrokerExceptionCode.INTERNAL_SERVER_ERROR, "try to get handle failed");
            }
            try {
                if (handleData.isNeedRemove()) {
                    return null;
                }
                res.set(handleData.getMessageReceiptHandle());
            } finally {
                handleData.unlock();
            }
            return handleData;
        });
        return res.get();
    }

    public MessageReceipt remove(String msgID, String handle) {
        Map<HandleKey, HandleData> handleMap = this.receiptHandleMap.get(msgID);
        if (handleMap == null) {
            return null;
        }
        long timeout = this.messageConfig.getLockTimeoutMsInHandleGroup();
        AtomicReference<MessageReceipt> res = new AtomicReference<>();
        handleMap.computeIfPresent(new HandleKey(handle), (handleKey, handleData) -> {
            if (!handleData.lock(timeout)) {
                throw new BrokerException(BrokerExceptionCode.INTERNAL_SERVER_ERROR, "try to remove and get handle failed");
            }
            try {
                if (!handleData.isNeedRemove()) {
                    handleData.setNeedRemove(true);
                    res.set(handleData.getMessageReceipt());
                }
                return null;
            } finally {
                handleData.unlock();
            }
        });
        removeHandleMapKeyIfNeed(msgID);
        return res.get();
    }

    public MessageReceipt removeOne(String msgID) {
        Map<HandleKey, HandleData> handleMap = this.receiptHandleMap.get(msgID);
        if (handleMap == null) {
            return null;
        }
        Set<HandleKey> keys = handleMap.keySet();
        for (HandleKey key : keys) {
            MessageReceipt res = this.remove(msgID, key.getOriginalHandle());
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    public void computeIfPresent(String msgID, String handle,
        Function<MessageReceipt, CompletableFuture<MessageReceipt>> function) {
        Map<HandleKey, HandleData> handleMap = this.receiptHandleMap.get(msgID);
        if (handleMap == null) {
            return;
        }
        long timeout = this.messageConfig.getLockTimeoutMsInHandleGroup();
        handleMap.computeIfPresent(new HandleKey(handle), (handleKey, handleData) -> {
            if (!handleData.lock(timeout)) {
                throw new BrokerException(BrokerExceptionCode.INTERNAL_SERVER_ERROR, "try to compute failed");
            }
            CompletableFuture<MessageReceipt> future = function.apply(handleData.getMessageReceipt());
            future.whenComplete((messageReceiptHandle, throwable) -> {
                try {
                    if (throwable != null) {
                        return;
                    }
                    if (messageReceiptHandle == null) {
                        handleData.setNeedRemove(true);
                    } else {
                        handleData.setMessageReceipt(messageReceiptHandle);
                    }
                } finally {
                    handleData.unlock();
                }
                if (handleData.isNeedRemove()) {
                    handleMap.remove(handleKey, handleData);
                }
                removeHandleMapKeyIfNeed(msgID);
            });
            return handleData;
        });
    }

    protected void removeHandleMapKeyIfNeed(String msgID) {
        this.receiptHandleMap.computeIfPresent(msgID, (msgIDKey, handleMap) -> {
            if (handleMap.isEmpty()) {
                return null;
            }
            return handleMap;
        });
    }

    public interface DataScanner {
        void onData(String msgID, String handle, MessageReceipt receiptHandle);
    }

    public void scan(DataScanner scanner) {
        this.receiptHandleMap.forEach((msgID, handleMap) -> {
            handleMap.forEach((handleKey, v) -> {
                scanner.onData(msgID, handleKey.getOriginalHandle(), v.getMessageReceipt());
            });
        });
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("receiptHandleMap", receiptHandleMap)
            .toString();
    }
}
