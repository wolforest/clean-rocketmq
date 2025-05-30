package cn.coderule.minimq.broker.domain.transaction;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.domain.domain.model.cluster.RequestContext;
import java.util.List;


public interface TransactionService {

    void addTransactionSubscription(RequestContext ctx, String group, List<String> topicList);

    void addTransactionSubscription(RequestContext ctx, String group, String topic);

    void replaceTransactionSubscription(RequestContext ctx, String group, List<String> topicList);

    void unSubscribeAllTransactionTopic(RequestContext ctx, String group);

    TransactionData addTransactionDataByBrokerAddr(RequestContext ctx, String brokerAddr, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        MessageBO message);

    TransactionData addTransactionDataByBrokerName(RequestContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        MessageBO message);

    EndTransactionRequestData genEndTransactionRequestHeader(RequestContext ctx, String topic, String producerGroup, Integer commitOrRollback,
        boolean fromTransactionCheck, String msgId, String transactionId);

    void onSendCheckTransactionStateFailed(RequestContext context, String producerGroup, TransactionData transactionData);
}
