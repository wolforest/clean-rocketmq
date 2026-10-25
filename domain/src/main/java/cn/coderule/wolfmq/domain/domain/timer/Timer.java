package cn.coderule.wolfmq.domain.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.domain.timer.model.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.model.TimerEvent;

public interface Timer extends Lifecycle {
    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

    void recover();

}
