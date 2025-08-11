package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import cn.coderule.minimq.domain.utils.MessageUtils;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.Collection;
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
    private int messageSize;

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

    private Long tagsCode;
    private String propertiesString;

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

    public String getBodyString() {
        if (body == null) {
            return "";
        }

        return new String(body);
    }

    public boolean isValid() {
        return MessageStatus.FOUND.equals(status);
    }

    public boolean isEmpty() {
        return 0 == this.messageSize;
    }

    public long getTagsCode() {
        if (null != this.tagsCode) {
            return this.tagsCode;
        }

        if (MapUtil.isEmpty(this.getProperties())) {
            this.tagsCode = 0L;
            return 0;
        }

        String tags = this.getProperties().get(MessageConst.PROPERTY_TAGS);
        this.tagsCode = MessageUtils.getTagsCode(tags);
        return this.tagsCode;
    }

    public String getPropertiesString() {
        if (null != this.propertiesString) {
            return this.propertiesString;
        }

        this.propertiesString = MessageUtils.propertiesToString(this.getProperties());
        return this.propertiesString;
    }

    public String getKeys() {
        return this.getProperty(MessageConst.PROPERTY_KEYS);
    }

    public void setKeys(Collection<String> keyCollection) {
        String keys = String.join(MessageConst.KEY_SEPARATOR, keyCollection);

        this.setKeys(keys);
    }

    public void setKeys(String keys) {
        this.putProperty(MessageConst.PROPERTY_KEYS, keys);
    }


    public String getShardingKey() {
        return this.getProperty(MessageConst.PROPERTY_SHARDING_KEY);
    }

    public String getTags() {
        return this.getProperty(MessageConst.PROPERTY_TAGS);
    }

    public void setTags(String tags) {
        this.putProperty(MessageConst.PROPERTY_TAGS, tags);
    }

    public String getMessageId() {
        return this.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
    }

    public void setMessageId(String tags) {
        this.putProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX, tags);
    }

    public TagType getTagType() {
        if ((this.sysFlag & MessageSysFlag.MULTI_TAGS_FLAG) == MessageSysFlag.MULTI_TAGS_FLAG) {
            return TagType.MULTI_TAG;
        }

        return TagType.SINGLE_TAG;
    }

    public void setReconsumeTime(String reconsumeTimes) {
        putProperty(MessageConst.PROPERTY_RECONSUME_TIME, reconsumeTimes);
    }

    public String getReconsumeTime() {
        return this.getProperty(MessageConst.PROPERTY_RECONSUME_TIME);
    }

    public void setMaxReconsumeTimes(String maxReconsumeTimes) {
        putProperty(MessageConst.PROPERTY_MAX_RECONSUME_TIMES, maxReconsumeTimes);
    }

    public String getMaxReconsumeTimes() {
        return this.getProperty(MessageConst.PROPERTY_MAX_RECONSUME_TIMES);
    }

    public String getClusterName() {
        return this.getProperty(MessageConst.PROPERTY_CLUSTER);
    }

    public void setClusterName(String clusterName) {
        this.putProperty(MessageConst.PROPERTY_CLUSTER, clusterName);
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

    /**
     * set message deliver time
     * deliverTime =
     *  * popTime + invisibleTime
     *  * reviveTime - PopConstants.ackTimeInterval
     *  * reviveTime
     * reviveTime = popTime + invisibleTime
     * @param timeMs  deliver time
     */
    public void setDeliverTime(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS, String.valueOf(timeMs));
    }

    public long getDeliverTime() {
        String t = this.getProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS);
        if (StringUtil.isBlank(t)) {
            return 0;
        }

        try {
            return Long.parseLong(t);
        } catch (Exception e) {
            return 0;
        }
    }

    public void setDelayTime(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELAY_MS, String.valueOf(timeMs));
    }

    public long getDelayTime() {
        String t = this.getProperty(MessageConst.PROPERTY_TIMER_DELAY_MS);
        if (StringUtil.isBlank(t)) {
            return 0;
        }

        try {
            return Long.parseLong(t);
        } catch (Exception e) {
            return 0;
        }
    }

    public long getTransactionCheckTime() {
        String t = this.getProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS);
        if (StringUtil.isBlank(t)) {
            return 0;
        }

        try {
            return Long.parseLong(t);
        } catch (Exception e) {
            return 0;
        }
    }
}
