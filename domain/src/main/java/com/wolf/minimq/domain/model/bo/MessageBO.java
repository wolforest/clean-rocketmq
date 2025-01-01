package com.wolf.minimq.domain.model.bo;

import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.model.Message;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MessageBO extends Message implements Serializable {
    private String brokerName;
    /**
     * set by MQ client
     */
    private int queueId;
    private String topicKey;

    private int storeSize;

    /**
     * set by consumeQueue while putting process of CommitLog
     */
    private long queueOffset;
    private int sysFlag;
    private long bornTimestamp;
    private SocketAddress bornHost;

    private long storeTimestamp;
    private SocketAddress storeHost;
    private String msgId;
    private long commitLogOffset;
    private int bodyCRC;

    /**
     *
     * in pop message mode:
     *  - client can change this value while retrying
     *  - broker also change this value to revive the message
     *  - message will send to DLG after reconsumeTimes greater than a special value
     */
    private int reconsumeTimes;
    private long preparedTransactionOffset;

    private MessageVersion version = MessageVersion.V1;
}
