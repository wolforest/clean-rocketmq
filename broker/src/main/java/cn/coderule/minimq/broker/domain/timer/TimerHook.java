package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.minimq.domain.core.enums.code.InvalidCode;
import cn.coderule.minimq.domain.core.exception.InvalidRequestException;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.ProduceContext;
import cn.coderule.minimq.domain.service.broker.produce.ProduceHook;
import lombok.extern.slf4j.Slf4j;

import static cn.coderule.minimq.domain.domain.timer.TimerConstants.TIMER_TOPIC;

@Slf4j
public class TimerHook implements ProduceHook {
    @Override
    public String hookName() {
        return TimerHook.class.getSimpleName();
    }

    @Override
    public void preProduce(ProduceContext context) {
        MessageBO messageBO = context.getMessageBO();
        if (!messageBO.isNormalOrCommitMessage()) {
            return;
        }

        if (isTimerTopic(messageBO)) {
            return;
        }

        if (!hasTimerProperty(messageBO)) {
            return;
        }

        transformMessage(context);
    }

    @Override
    public void postProduce(ProduceContext context) {

    }

    private boolean isTimerTopic(MessageBO msg) {
        return TIMER_TOPIC.equals(msg.getTopic());
    }

    private boolean hasTimerProperty(MessageBO msg) {
        if (isTimerTopic(msg) || msg.getTimeout() > 0) {
            return false;
        }

        return msg.getDeliverTime() > 0 || msg.getDelayTime() > 0;
    }

    private void transformMessage(ProduceContext context) {
        MessageBO messageBO = context.getMessageBO();
    }

    private long getDeliverTime(MessageBO messageBO) {
        long time = messageBO.getDeliverTime();
        if (time > 0) {
            return time;
        }

        time = messageBO.getDelayTime();
        if (time > 0) {
            return System.currentTimeMillis() + time;
        }

        time = messageBO.getDelaySecond();
        if (time > 0) {
            return System.currentTimeMillis() + time * 1000;
        }

        log.error("message deliver time is invalid {}", messageBO);
        throw new InvalidRequestException(InvalidCode.ILLEGAL_DELIVERY_TIME, "message deliver time is invalid");
    }
}
