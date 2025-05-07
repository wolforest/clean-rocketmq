package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.common.core.RequestContext;

public class QueueSelector {
    public MessageQueue select(RequestContext context, MessageBO messageBO) {
        return null;
    }
}
