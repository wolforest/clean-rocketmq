package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.wolfmq.broker.domain.meta.RouteService;
import cn.coderule.wolfmq.domain.core.enums.code.BrokerExceptionCode;
import cn.coderule.wolfmq.domain.core.exception.BrokerException;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.cluster.selector.MessageQueueView;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
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
            throw new BrokerException(
                BrokerExceptionCode.FORBIDDEN, "can not find route of topic"
            );
        }

        try {
            return selectByShardingKey(messageBO, queueView);
        } catch (Exception e) {
            log.error("select queue error", e);
            throw new BrokerException(
                BrokerExceptionCode.INTERNAL_SERVER_ERROR, "select queue error"
            );
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
