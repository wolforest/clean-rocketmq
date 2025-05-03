/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.coderule.minimq.rpc.broker.transaction;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.common.core.RequestContext;
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
