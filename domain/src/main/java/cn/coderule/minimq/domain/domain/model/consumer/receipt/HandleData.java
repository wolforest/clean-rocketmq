package cn.coderule.minimq.domain.domain.model.consumer.receipt;

import cn.coderule.common.util.lang.BeanUtil;
import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Data;

@Data
public class HandleData implements Serializable {
    private final Semaphore semaphore = new Semaphore(1);
    private volatile boolean needRemove = false;
    private volatile MessageReceipt messageReceipt;

    public HandleData(MessageReceipt messageReceipt) {
        this.messageReceipt = messageReceipt;
    }

    public boolean lock(long timeoutMs) {
        try {
            return this.semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return false;
        }
    }

    public void unlock() {
        this.semaphore.release();
    }

    public MessageReceipt getMessageReceiptHandle() {
        return messageReceipt;
    }

    @Override
    public int hashCode() {
        return BeanUtil.hashCode(semaphore, needRemove, messageReceipt);
    }

    @Override
    public String toString() {
        return BeanUtil.toStringBuilder(this)
            .append("semaphore", semaphore)
            .append("needRemove", needRemove)
            .append("messageReceiptHandle", messageReceipt)
            .toString();
    }
}
