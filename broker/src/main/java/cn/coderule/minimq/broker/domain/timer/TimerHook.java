package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.producer.ProduceContext;
import cn.coderule.minimq.domain.service.broker.produce.ProduceHook;

import static cn.coderule.minimq.domain.domain.timer.TimerConstants.TIMER_TOPIC;

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
}
