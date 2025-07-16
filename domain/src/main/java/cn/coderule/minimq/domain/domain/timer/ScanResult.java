package cn.coderule.minimq.domain.domain.timer;

import java.io.Serializable;
import java.util.LinkedList;
import lombok.Data;

@Data
public class ScanResult implements Serializable {
    LinkedList<TimerEvent> normalMsgStack = new LinkedList<>();
    LinkedList<TimerEvent> deleteMsgStack = new LinkedList<>();
    int code = 0;

    public void addDeleteMsgStack(TimerEvent timerEvent) {
        deleteMsgStack.add(timerEvent);
    }

    public void addNormalMsgStack(TimerEvent timerEvent) {
        normalMsgStack.addFirst(timerEvent);
    }

    public int sizeOfDeleteMsgStack() {
        return deleteMsgStack.size();
    }

    public int sizeOfNormalMsgStack() {
        return normalMsgStack.size();
    }
}
