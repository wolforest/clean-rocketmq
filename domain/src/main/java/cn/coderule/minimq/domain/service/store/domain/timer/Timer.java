package cn.coderule.minimq.domain.service.store.domain.timer;

import cn.coderule.minimq.domain.domain.timer.TimerCheckpoint;

public interface Timer {
    void storeCheckpoint(TimerCheckpoint checkpoint);
    TimerCheckpoint loadCheckpoint();
}
