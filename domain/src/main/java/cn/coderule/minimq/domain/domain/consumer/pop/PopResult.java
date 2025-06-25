package cn.coderule.minimq.domain.domain.model.consumer.pop;

import cn.coderule.minimq.domain.domain.core.enums.consume.PopStatus;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class PopResult implements Serializable {
    private List<MessageBO> msgFoundList;
    private PopStatus popStatus;
    private long popTime;
    private long invisibleTime;
    private long restNum;

    public PopResult(PopStatus popStatus, List<MessageBO> msgFoundList) {
        this.popStatus = popStatus;
        this.msgFoundList = msgFoundList;
    }


    public boolean isEmpty() {
        return msgFoundList == null || msgFoundList.isEmpty();
    }
}
