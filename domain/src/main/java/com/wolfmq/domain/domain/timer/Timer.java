package com.wolfmq.domain.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.domain.domain.timer.model.ScanResult;
import com.wolfmq.domain.domain.timer.model.TimerEvent;

public interface Timer extends Lifecycle {
    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

    void recover();

}
