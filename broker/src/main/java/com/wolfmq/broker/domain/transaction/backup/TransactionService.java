package com.wolfmq.broker.domain.transaction.backup;

import com.wolfmq.rpc.common.core.relay.response.TransactionResult;
import com.wolfmq.domain.domain.message.MessageBO;
import com.wolfmq.domain.server.RequestContext;
import java.util.List;


public interface TransactionService {

    void addTransactionSubscription(RequestContext ctx, String group, List<String> topicList);

    void addTransactionSubscription(RequestContext ctx, String group, String topic);

    void replaceTransactionSubscription(RequestContext ctx, String group, List<String> topicList);

    void unSubscribeAllTransactionTopic(RequestContext ctx, String group);

    TransactionResult addTransactionDataByBrokerAddr(RequestContext ctx, String brokerAddr, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        MessageBO message);

    TransactionResult addTransactionDataByBrokerName(RequestContext ctx, String brokerName, String topic, String producerGroup, long tranStateTableOffset, long commitLogOffset, String transactionId,
        MessageBO message);

    EndTransactionRequestData genEndTransactionRequestHeader(RequestContext ctx, String topic, String producerGroup, Integer commitOrRollback,
        boolean fromTransactionCheck, String msgId, String transactionId);

    void onSendCheckTransactionStateFailed(RequestContext context, String producerGroup, TransactionResult transactionResult);
}
