package cn.coderule.minimq.domain.domain.store.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public interface Timer extends Lifecycle {
    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

}
