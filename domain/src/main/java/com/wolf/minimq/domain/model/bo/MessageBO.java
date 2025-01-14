package com.wolf.minimq.domain.model.bo;

import com.wolf.common.util.collection.MapUtil;
import com.wolf.common.util.lang.StringUtil;
import com.wolf.minimq.domain.constant.SysFlag;
import com.wolf.minimq.domain.enums.MessageConst;
import com.wolf.minimq.domain.enums.MessageStatus;
import com.wolf.minimq.domain.enums.MessageVersion;
import com.wolf.minimq.domain.enums.TagType;
import com.wolf.minimq.domain.model.Message;
import java.io.Serializable;
import java.net.SocketAddress;
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
    private MessageStatus status;

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

    public boolean isValid() {
        return MessageStatus.FOUND.equals(status);
    }

    public boolean isEmpty() {
        return 0 == this.storeSize;
    }

    public long getTagsCode() {
        if (MapUtil.isEmpty(this.getProperties())) {
            return 0;
        }

        String tags = this.getProperties().get(MessageConst.PROPERTY_TAGS);
        if (StringUtil.isBlank(tags)) {
            return 0;
        }

        return tags.hashCode();
    }

    public TagType getTagType() {
        if ((this.sysFlag & SysFlag.MULTI_TAGS_FLAG) == SysFlag.MULTI_TAGS_FLAG) {
            return TagType.MULTI_TAG;
        }

        return TagType.SINGLE_TAG;
    }

    public boolean isWaitStore() {
        String result = this.getProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK);
        if (null == result) {
            return true;
        }

        return Boolean.parseBoolean(result);
    }

    public void setWaitStore(boolean waitStoreMsgOK) {
        this.putProperty(MessageConst.PROPERTY_WAIT_STORE_MSG_OK, Boolean.toString(waitStoreMsgOK));
    }
}
