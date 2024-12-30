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
package com.wolf.minimq.domain.model.dto;

import java.io.Serializable;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Async CommitLog dispatching core object
 *
 * created By ReputMessageService
 * dispatch to :
 *     1. CommitLogDispatcherBuildConsumeQueue
 *          -> ConsumeQueueStore.putMessagePositionInfoWrapper()
 *     2. CommitLogDispatcherBuildIndex
 *          -> IndexService.buildIndex()
 *     3. CommitLogDispatcherCompaction
 *          -> CompactionService.putRequest()
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommitLogEvent implements Serializable {
    private String topic;
    private int queueId;
    private long commitLogOffset;
    private int msgSize;
    /**
     * from message.propertiesMap, possible key are below:
     *    1. MessageConst.PROPERTY_TAGS
     *    2. MessageConst.PROPERTY_DELAY_TIME_LEVEL
     */
    private long tagsCode;
    private long storeTimestamp;
    private long consumeQueueOffset;
    /**
     * message unique key
     * stored in message.properties["UNIQ_KEY"]
     * MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX = "UNIQ_KEY"
     */
    private String uniqKey;
    /**
     * message keys
     * stored in message.properties["KEYS"]
     * MessageConst.PROPERTY_KEYS = "KEYS"
     */
    private String keys;
    private boolean success;

    private int sysFlag;
    private long preparedTransactionOffset;
    private Map<String, String> propertiesMap;
    private byte[] bitMap;

    //the buffer size maybe larger than the msg size if the message is wrapped by something
    private int bufferSize = -1;

    // for batch consume queue
    private long  msgBaseOffset = -1;
    private short batchSize = 1;

    private long nextReputFromOffset = -1;
    private String offsetId;

    @Override
    public String toString() {
        return "DispatchRequest{" +
                "topic='" + topic + '\'' +
                ", queueId=" + queueId +
                ", commitLogOffset=" + commitLogOffset +
                ", msgSize=" + msgSize +
                ", success=" + success +
                ", msgBaseOffset=" + msgBaseOffset +
                ", batchSize=" + batchSize +
                ", nextReputFromOffset=" + nextReputFromOffset +
            '}';
    }
}
