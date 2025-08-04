package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.domain.config.business.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.wheel.Block;
import cn.coderule.minimq.domain.domain.timer.wheel.Slot;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskScheduler {
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public TaskScheduler(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.timerConfig = storeConfig.getTimerConfig();
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
    }

    public boolean addTimer(TimerEvent event) {
        log.debug("add timer event: delayTime={}, message={}",
            DateUtil.asLocalDateTime(event.getDelayTime()),
            event.getMessageBO()
        );

        int magic = TimerConstants.MAGIC_DEFAULT;
        long delayTime = event.getDelayTime();
        if (needRoll(event)) {
            magic = magic | TimerConstants.MAGIC_ROLL;
            delayTime = getRolledDelayedTime(event.getBatchTime(), delayTime);
        }

        magic = addDeleteFlag(event.getMessageBO(), magic);
        Slot slot = timerWheel.getSlot(event.getDelayTime());

        long timerLogOffset = appendTimerLog(event, delayTime, magic, slot);
        if (-1 == timerLogOffset) {
            return false;
        }

        putWheelSlot(timerLogOffset, delayTime, slot, event);

        return true;
    }

    private boolean needRoll(TimerEvent event) {
        return event.getDelayTime() - event.getBatchTime()
            > (long) timerConfig.getWheelSlots() * timerConfig.getPrecision();
    }

    private long getRolledDelayedTime(long batchTime, long delayedTime) {
        int wheelSlots = timerConfig.getWheelSlots();
        int precision = timerConfig.getPrecision();
        if (delayedTime - batchTime - (long) wheelSlots * precision < (long) wheelSlots / 3 * precision) {
            // if delayedTime less than 4/3 times timerWheel slots
            // set delayedTime to 1/2 times timeWheel slots * precision
            // for example:
            // if timerWheel slots is 2 days
            // delayedTime between 2days and 2.667 days
            // the delayedTime will set to slot corresponding to 1 day

            //give enough time to next roll
            return batchTime + (long) (wheelSlots / 2) * precision;
        }

        // else set delayedTime to timerWheel slots * precision
        // for example:
        // if timerWheel slots is 2 days
        // the delayedTime will be set to slot corresponding to 2day
        return batchTime + (long) wheelSlots * precision;
    }

    private int addDeleteFlag(MessageBO messageBO, int magic) {
        if (!isDelete(messageBO)) {
            return magic;
        }

        return magic | TimerConstants.MAGIC_DELETE;
    }

    private long appendTimerLog(TimerEvent event, long delayTime, int magic, Slot slot) {
        String realTopic = event.getMessageBO().getProperty(MessageConst.PROPERTY_REAL_TOPIC);
        int realDelayTime = (int) (delayTime - event.getBatchTime());
        Block block = Block.builder()
            .size(Block.SIZE)
            .prevPos(slot.getLastPos())
            .magic(magic)
            .currWriteTime(event.getBatchTime())
            .delayedTime(realDelayTime)
            .offsetPy(event.getCommitLogOffset())
            .sizePy(event.getMessageSize())
            .hashCodeOfRealTopic(getTopicHashCode(realTopic))
            .reservedValue(0)
            .build();

        return timerLog.append(block, 0 , Block.SIZE);
    }

    private void putWheelSlot(long timerLogOffset, long delayTime, Slot slot, TimerEvent event) {
        long firstPos = -1 == slot.getFirstPos()
            ? timerLogOffset
            : slot.getFirstPos();

        int num = isDelete(event.getMessageBO())
            ? slot.getNum() - 1
            : slot.getNum() + 1;

        timerWheel.putSlot(delayTime, firstPos, timerLogOffset, num, slot.getMagic());
    }

    private boolean isDelete(MessageBO messageBO) {
        String key = messageBO.getProperty(TimerConstants.TIMER_DELETE_UNIQUE_KEY);
        return key != null;
    }

    public int getTopicHashCode(String topic) {
        return null == topic ? 0 : topic.hashCode();
    }

}
