package com.wolf.minimq.domain.model.vo;

import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.model.Message;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class MessageContext implements Serializable {
    private List<Message> messageList = new ArrayList<>();

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

    private String propertiesString;
    private long tagsCode;
    private ByteBuffer encodedBuff;
    private volatile boolean encodeCompleted;
    private MessageVersion version = MessageVersion.V1;

    public Message getMessage() {
        return messageList.getFirst();
    }
}
