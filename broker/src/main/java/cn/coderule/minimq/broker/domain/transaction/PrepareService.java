package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.domain.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.model.producer.EnqueueResult;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.utils.MessageUtils;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrepareService {
    public CompletableFuture<EnqueueResult> prepare(RequestContext context, MessageBO messageBO) {
        return null;
    }

    private MessageBO buildPrepareMessage(MessageBO msg) {
        String uniqId = msg.getMessageId();
        if (uniqId != null && !uniqId.isEmpty()) {
            msg.putProperty(TransactionUtil.TRANSACTION_ID, uniqId);
        }

        msg.putProperty(MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
        msg.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));

        //reset msg transaction type and set topic = TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC
        msg.setSysFlag(MessageSysFlag.resetTransactionValue(msg.getSysFlag(), MessageSysFlag.TRANSACTION_NOT_TYPE));
        msg.setTopic(TransactionUtil.buildHalfTopic());
        msg.setQueueId(0);
        msg.setPropertiesString(MessageUtils.propertiesToString(msg.getProperties()));
        return msg;
    }
}
