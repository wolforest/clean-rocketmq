package cn.coderule.minimq.broker.domain.timer;

import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
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

        int sysFlag = messageBO.getSysFlag();
        int transactionType = MessageSysFlag.getTransactionValue(sysFlag);

    }

    @Override
    public void postProduce(ProduceContext context) {

    }

    private boolean isRolledTimerMessage(MessageBO msg) {
        return TIMER_TOPIC.equals(msg.getTopic());
    }
}
