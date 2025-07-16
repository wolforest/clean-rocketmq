package cn.coderule.minimq.store.domain.timer;

import cn.coderule.minimq.domain.domain.timer.TimerCheckpoint;
import cn.coderule.minimq.domain.service.store.domain.timer.Timer;

public class DefaultTimer implements Timer {
    @Override
    public void storeCheckpoint(TimerCheckpoint checkpoint) {

    }

    @Override
    public TimerCheckpoint loadCheckpoint() {
        return null;
    }
}
