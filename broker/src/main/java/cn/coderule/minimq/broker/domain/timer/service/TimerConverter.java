package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public class TimerConverter {
    public static TimerEvent toEvent(MessageBO messageBO, long enqueueTime, int magic) {
        long delayTime = getDelayTime(messageBO);

        return TimerEvent.builder()
            .commitLogOffset(messageBO.getCommitLogOffset())
            .messageSize(messageBO.getMessageSize())
            .delayTime(delayTime)
            .magic(magic)
            .enqueueTime(enqueueTime)
            .messageBO(messageBO)
            .build();
    }

    private static long getDelayTime(MessageBO messageBO) {
        String delayString = messageBO.getProperty(MessageConst.PROPERTY_TIMER_OUT_MS);
        return Long.parseLong(delayString);
    }
}
