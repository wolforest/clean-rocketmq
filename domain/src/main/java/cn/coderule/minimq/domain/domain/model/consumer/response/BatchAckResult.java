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

package cn.coderule.minimq.domain.domain.model.consumer.response;

import cn.coderule.minimq.domain.domain.exception.BrokerException;
import cn.coderule.minimq.domain.domain.model.consumer.receipt.MessageIdReceipt;

public class BatchAckResult {

    private final MessageIdReceipt messageIdReceipt;
    private AckResult ackResult;
    private BrokerException brokerException;

    public BatchAckResult(MessageIdReceipt messageIdReceipt,
        AckResult ackResult) {
        this.messageIdReceipt = messageIdReceipt;
        this.ackResult = ackResult;
    }

    public BatchAckResult(MessageIdReceipt messageIdReceipt,
        BrokerException brokerException) {
        this.messageIdReceipt = messageIdReceipt;
        this.brokerException = brokerException;
    }

    public MessageIdReceipt getReceiptHandleMessage() {
        return messageIdReceipt;
    }

    public AckResult getAckResult() {
        return ackResult;
    }

    public BrokerException getBrokerException() {
        return brokerException;
    }
}
