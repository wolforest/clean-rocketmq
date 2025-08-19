package cn.coderule.minimq.domain.domain.consumer.consume.pop;

import cn.coderule.minimq.domain.core.enums.consume.PopStatus;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopResult implements Serializable {
    @Builder.Default
    private List<MessageBO> messageList = new ArrayList<>();
    private PopStatus popStatus;
    private long popTime;
    private long invisibleTime;
    private long restNum;

    public static CompletableFuture<PopResult> future() {
        return CompletableFuture.completedFuture(
            new PopResult(PopStatus.NO_NEW_MSG, List.of())
        );
    }

    public PopResult(PopStatus popStatus, List<MessageBO> messageList) {
        this.popStatus = popStatus;
        this.messageList = messageList;
    }

    public boolean isEmpty() {
        return messageList == null || messageList.isEmpty();
    }

    public boolean hasFound() {
        return popStatus == PopStatus.FOUND;
    }

    public int countMessage() {
        return messageList == null ? 0 : messageList.size();
    }
}
