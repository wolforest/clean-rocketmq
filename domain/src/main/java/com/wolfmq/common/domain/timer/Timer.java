package com.wolfmq.common.domain.timer;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.domain.timer.model.ScanResult;
import com.wolfmq.common.domain.timer.model.TimerEvent;

public interface Timer extends Lifecycle {
    boolean addTimer(TimerEvent event);
    ScanResult scan(long delayTime);

    void recover();

}
