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
        return recreateMessage(messageBO, needRoll);
    }

    private static MessageBO recreateMessage(MessageBO message, boolean needRoll) {
        MessageBO newMessage = MessageBO.builder()
            .body(message.getBody())
            .flag(message.getFlag())
            .properties(message.getProperties())
            .tagsCode(message.getTagsCode())
            .propertiesString(message.getPropertiesString())
            .sysFlag(message.getSysFlag())
            .bornTimestamp(message.getBornTimestamp())
            .bornHost(message.getBornHost())
            .storeHost(message.getStoreHost())
            .reconsumeTimes(message.getReconsumeTimes())
            .build();

        newMessage.setWaitStore(false);
        setTopicAndQueueId(message, newMessage,needRoll);

        return newMessage;
    }

    private static void setTopicAndQueueId(MessageBO message, MessageBO newMessage, boolean needRoll) {
        if (needRoll) {
            newMessage.setTopic(message.getTopic());
            newMessage.setQueueId(message.getQueueId());
            return;
        }

        String topic = message.getProperty(MessageConst.PROPERTY_REAL_TOPIC);
        String queueIdStr = message.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        int queueId = Integer.parseInt(queueIdStr);

        newMessage.setTopic(topic);
        newMessage.setQueueId(queueId);

        newMessage.removeProperty(MessageConst.PROPERTY_REAL_TOPIC);
        newMessage.removeProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
    }
    private static long getDelayTime(MessageBO messageBO) {
        String delayString = messageBO.getProperty(MessageConst.PROPERTY_TIMER_OUT_MS);
        return Long.parseLong(delayString);
    }
}
