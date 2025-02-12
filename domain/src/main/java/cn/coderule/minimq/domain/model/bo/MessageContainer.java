package cn.coderule.minimq.domain.model.bo;

import cn.coderule.minimq.domain.enums.MessageVersion;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageContainer implements Serializable {
    private List<MessageBO> messageList = new ArrayList<>();

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

    @Builder.Default
    private MessageVersion version = MessageVersion.V1;
}
