package cn.coderule.wolfmq.domain.domain.store.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;

public interface Timer extends Lifecycle {
    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

    void recover();

}
