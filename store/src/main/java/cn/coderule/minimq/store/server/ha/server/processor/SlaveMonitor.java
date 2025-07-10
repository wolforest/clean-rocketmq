package cn.coderule.minimq.store.server.ha.server.processor;

import java.io.Serializable;
import lombok.Data;

@Data
public class SlaveMonitor implements Serializable {
    private volatile long requestOffset = -1;
    private volatile long ackOffset = -1;
}
