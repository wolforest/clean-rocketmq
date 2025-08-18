package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.core.enums.consume.PopStatus;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.Data;

@Data
public class PopResult implements Serializable {
    private List<MessageBO> msgFoundList;
    private PopStatus popStatus;
    private long popTime;
    private long invisibleTime;
    private long restNum;

    public static CompletableFuture<PopResult> future() {
        return CompletableFuture.completedFuture(
            new PopResult(PopStatus.NO_NEW_MSG, List.of())
        );
    }

    public PopResult(PopStatus popStatus, List<MessageBO> msgFoundList) {
        this.popStatus = popStatus;
        this.msgFoundList = msgFoundList;
    }

    public boolean isEmpty() {
        return msgFoundList == null || msgFoundList.isEmpty();
    }

    public boolean hasFound() {
        return popStatus == PopStatus.FOUND;
    }
}
