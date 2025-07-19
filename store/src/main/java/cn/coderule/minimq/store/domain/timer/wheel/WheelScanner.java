package cn.coderule.minimq.store.domain.timer.wheel;

import cn.coderule.minimq.domain.config.TimerConfig;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.cluster.store.SelectedMappedBuffer;
import cn.coderule.minimq.domain.domain.timer.ScanResult;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.domain.timer.wheel.Slot;
import cn.coderule.minimq.domain.utils.TimerUtils;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WheelScanner {
    private final StoreConfig storeConfig;
    private final TimerConfig timerConfig;
    private final TimerLog timerLog;
    private final TimerWheel timerWheel;

    public WheelScanner(StoreConfig storeConfig, TimerLog timerLog, TimerWheel timerWheel) {
        this.storeConfig = storeConfig;
        this.timerConfig = storeConfig.getTimerConfig();
        this.timerLog = timerLog;
        this.timerWheel = timerWheel;
    }

    public ScanResult scan(long delayTime) {
        ScanResult result = new ScanResult();
        Slot slot = timerWheel.getSlot(delayTime);
        if (-1 == slot.getTimeMs()) {
            return result;
        }

        result.setCode(1);

        try {
            scanBySlot(result, slot);
        } catch (Throwable e) {
            log.error("scan timer log error", e);
        }

        return result;
    }

    private void scanBySlot(ScanResult result, Slot slot) {
        long currentOffset = slot.getLastPos();
        Set<String> deleteKeys = new ConcurrentSkipListSet<>();
        LinkedList<SelectedMappedBuffer> bufferList = new LinkedList<>();
        SelectedMappedBuffer buffer = null;

        while (-1 != currentOffset) {
            buffer = scanTimeLog(buffer, currentOffset, bufferList);
            if (buffer == null) {
                break;
            }

            currentOffset = addScanResult(result, currentOffset, buffer, deleteKeys);
        }
    }

    private SelectedMappedBuffer scanTimeLog(
        SelectedMappedBuffer buffer,
        long currentOffset,
        LinkedList<SelectedMappedBuffer> bufferList
    ) {
        if (null != buffer && buffer.getStartOffset() <= currentOffset) {
            return buffer;
        }

        buffer = timerLog.getWholeBuffer(currentOffset);
        if (null == buffer) {
            return null;
        }

        bufferList.add(buffer);
        return buffer;
    }

    private long addScanResult(
        ScanResult result,
        long currentOffset,
        SelectedMappedBuffer buffer,
        Set<String> deleteKeys
    ) {
        long prevPos = -1;

        try {
            ByteBuffer byteBuffer = buffer.getByteBuffer();

            prevPos = getPrevPos(byteBuffer, currentOffset);
            TimerEvent event = getTimerEvent(byteBuffer, deleteKeys);
            int magic = event.getMagic();

            if (TimerUtils.needDelete(magic) && !TimerUtils.needRoll(magic)) {
                result.addDeleteMsgStack(event);
            } else {
                result.addNormalMsgStack(event);
            }
        } catch (Exception e) {
            log.error("addScanResult error", e);
        } finally {
            currentOffset = prevPos;
        }

        return currentOffset;
    }

    private TimerEvent getTimerEvent(ByteBuffer byteBuffer, Set<String> deleteKeys) {
        int magic = byteBuffer.getInt();
        long enqueueTime = byteBuffer.getLong();
        long delayedTime = byteBuffer.getInt() + enqueueTime;
        long committedOffset = byteBuffer.getLong();
        int size = byteBuffer.getInt();

        return TimerEvent.builder()
            .magic(magic)
            .messageSize(size)
            .deleteList(deleteKeys)
            .delayTime(delayedTime)
            .enqueueTime(enqueueTime)
            .commitLogOffset(committedOffset)
            .build();
    }

    private long getPrevPos(ByteBuffer byteBuffer, long currentOffset) {
        int position = (int) (currentOffset % timerConfig.getTimerLogFileSize());
        byteBuffer.position(position);
        byteBuffer.getInt(); //size
        return byteBuffer.getLong();
    }
}
