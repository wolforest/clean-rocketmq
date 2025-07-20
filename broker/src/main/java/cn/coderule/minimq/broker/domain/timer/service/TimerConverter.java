package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;

public class TimerConverter {
    public static TimerEvent convert(MessageBO messageBO, long enqueueTime) {
        return TimerEvent.builder()
                .messageBO(messageBO)
                .build();
    }

    private static long getDelayTime(MessageBO messageBO) {
        String delayString = messageBO.getProperty(MessageConst.PROPERTY_TIMER_OUT_MS);
        return Long.parseLong(delayString);
    }
}
