package cn.coderule.minimq.broker.domain.producer;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.StringUtil;
import cn.coderule.minimq.broker.domain.meta.RouteService;
import cn.coderule.minimq.domain.domain.enums.code.BrokerExceptionCode;
import cn.coderule.minimq.domain.domain.exception.BrokerException;
import cn.coderule.minimq.domain.domain.model.MessageQueue;
import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.cluster.selector.MessageQueueView;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueueSelector {
    private final RouteService routeService;

    public QueueSelector(RouteService routeService) {
        this.routeService = routeService;
    }

    public MessageQueue select(RequestContext context, MessageBO messageBO) {
        MessageQueueView queueView = routeService.getQueueView(context, messageBO.getTopic());
        if (queueView == null) {
            throw new BrokerException(BrokerExceptionCode.FORBIDDEN, "can not find route of topic");
        }

        try {
            return selectByShardingKey(messageBO, queueView);
        } catch (Exception e) {
            log.error("select queue error", e);
            return null;
        }
    }

    private MessageQueue selectByShardingKey(MessageBO messageBO, MessageQueueView queueView) {
        String shardingKey = messageBO.getShardingKey();
        if (StringUtil.isBlank(shardingKey)) {
            return queueView
                .getWriteSelector()
                .selectOneByPipeline(false);
        }

        List<MessageQueue> messageQueueList = queueView.getWriteSelector().getQueues();
        int bucket = HashUtil.consistentHash(shardingKey.hashCode(), messageQueueList.size());
        return messageQueueList.get(bucket);
    }
}
