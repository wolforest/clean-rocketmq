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
package cn.coderule.minimq.rpc.broker.relay;

import cn.coderule.minimq.domain.domain.model.message.MessageBO;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumeMessageDirectlyResult;
import cn.coderule.minimq.rpc.broker.protocol.body.ConsumerRunningInfo;
import cn.coderule.minimq.rpc.broker.protocol.header.CheckTransactionStateRequestHeader;
import cn.coderule.minimq.rpc.broker.protocol.header.ConsumeMessageDirectlyResultRequestHeader;
import cn.coderule.minimq.rpc.broker.protocol.header.GetConsumerRunningInfoRequestHeader;
import cn.coderule.minimq.rpc.broker.transaction.TransactionData;
import cn.coderule.minimq.rpc.common.core.RequestContext;
import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import java.util.concurrent.CompletableFuture;

public interface RelayService {

    CompletableFuture<RelayResult<ConsumerRunningInfo>> processGetConsumerRunningInfo(
        RequestContext context,
        RpcCommand command,
        GetConsumerRunningInfoRequestHeader header
    );

    CompletableFuture<RelayResult<ConsumeMessageDirectlyResult>> processConsumeMessageDirectly(
        RequestContext context,
        RpcCommand command,
        ConsumeMessageDirectlyResultRequestHeader header
    );

    RelayData<TransactionData, Void> processCheckTransactionState(
        RequestContext context,
        RpcCommand command,
        CheckTransactionStateRequestHeader header,
        MessageBO messageBO
    );
}
