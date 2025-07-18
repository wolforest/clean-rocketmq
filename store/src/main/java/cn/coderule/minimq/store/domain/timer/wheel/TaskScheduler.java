package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.common.util.lang.time.DateUtil;
import cn.coderule.minimq.domain.config.TimerConfig;
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
    private final StoreConfig storeConfig;
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public TaskScheduler(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.storeConfig = storeConfig;
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
            // roll delay time
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
            .prevPos(slot.lastPos)
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
        long firstPos = -1 == slot.firstPos
            ? timerLogOffset
            : slot.firstPos;

        int num = isDelete(event.getMessageBO())
            ? slot.num - 1
            : slot.num + 1;

        timerWheel.putSlot(delayTime, firstPos, timerLogOffset, num, slot.magic);
    }

    private boolean isDelete(MessageBO messageBO) {
        String key = messageBO.getProperty(TimerConstants.TIMER_DELETE_UNIQUE_KEY);
        return key != null;
    }


    public int getTopicHashCode(String topic) {
        return null == topic ? 0 : topic.hashCode();
    }

}
