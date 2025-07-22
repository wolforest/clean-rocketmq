package cn.coderule.minimq.broker.domain.timer.context;

import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import java.io.Serializable;
import java.util.List;
import lombok.Data;

@Data
public class SplitContext implements Serializable {
    private int msgIndex = 0;
    private int fileIndex = -1;
    private List<TimerEvent> list = null;
}
