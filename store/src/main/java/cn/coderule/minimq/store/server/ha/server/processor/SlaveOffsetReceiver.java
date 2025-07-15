package cn.coderule.minimq.store.server.ha.server.processor;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.lang.concurrent.thread.ServiceThread;
import java.io.Serializable;
import lombok.Data;

@Data
public class SlaveOffsetReceiver extends ServiceThread implements Serializable, Lifecycle {
    private volatile long requestOffset = -1;
    private volatile long ackOffset = -1;

    @Override
    public String getServiceName() {
        return SlaveOffsetReceiver.class.getSimpleName();
    }

    @Override
    public void run() {

    }
}
