package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.cluster.store.domain.timer.Timer;
import cn.coderule.minimq.domain.config.store.StorePath;
import java.io.IOException;

public class DefaultTimer implements Timer {
    private final TaskAdder taskAdder;
    private final TaskScanner taskScanner;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;


    public DefaultTimer(StoreConfig storeConfig) throws IOException {
        TimerConfig timerConfig = storeConfig.getTimerConfig();

        this.timerLog = new TimerLog(
            StorePath.getTimerLogPath(),
            timerConfig.getTimerLogFileSize()
        );

        this.timerWheel = new TimerWheel(
            StorePath.getTimerWheelPath(),
            timerConfig.getTotalSlots(),
            timerConfig.getPrecision()
        );

        this.taskScanner = new TaskScanner(storeConfig, timerLog, timerWheel);
        this.taskAdder = new TaskAdder(storeConfig, timerLog, timerWheel);
    }

    @Override
    public void initialize() throws Exception {
        timerLog.load();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        timerWheel.shutdown();
        timerLog.shutdown();
    }

    @Override
    public boolean addTimer(TimerEvent event) {
        return taskAdder.addTimer(event);
    }

    @Override
    public ScanResult scan(long delayTime) {
        return taskScanner.scan(delayTime);
    }


}
