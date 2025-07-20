package cn.coderule.minimq.broker.domain.timer.service;

import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.timer.TimerConstants;
import cn.coderule.minimq.domain.domain.timer.TimerEvent;
import cn.coderule.minimq.domain.utils.TimerUtils;

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

    public static MessageBO toMessage(TimerEvent event) {
        MessageBO messageBO = event.getMessageBO();
        if (Long.MAX_VALUE == event.getEnqueueTime()) {
            messageBO.putProperty(TimerConstants.TIMER_ENQUEUE_MS, String.valueOf(Long.MAX_VALUE));
        } else if (-1 != event.getEnqueueTime()) {
            messageBO.putProperty(TimerConstants.TIMER_ENQUEUE_MS, event.getEnqueueTime() + "");
        }

        boolean needRoll = TimerUtils.needRoll(event.getMagic());
        if (needRoll) {
            if (null != messageBO.getProperty(TimerConstants.TIMER_ROLL_TIMES)) {
                int times = Integer.parseInt(messageBO.getProperty(TimerConstants.TIMER_ROLL_TIMES)) + 1;
                messageBO.putProperty(TimerConstants.TIMER_ROLL_TIMES, times + "");
            } else {
                messageBO.putProperty(TimerConstants.TIMER_ROLL_TIMES,   "1");
            }
        }

        messageBO.putProperty(TimerConstants.TIMER_ENQUEUE_MS, System.currentTimeMillis() + "");
        return messageBO;
    }

    private static MessageBO recreateMessage(MessageBO message, boolean needRoll) {
        MessageBO newMessage = MessageBO.builder()
            .build();

        if (needRoll) {
            newMessage.setTopic(message.getTopic());
            newMessage.setQueueId(message.getQueueId());
        } else {
            newMessage.setTopic(message.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
            newMessage.setQueueId(Integer.parseInt(message.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID)));

        }

        return newMessage;
    }

    private static long getDelayTime(MessageBO messageBO) {
        String delayString = messageBO.getProperty(MessageConst.PROPERTY_TIMER_OUT_MS);
        return Long.parseLong(delayString);
    }
}
