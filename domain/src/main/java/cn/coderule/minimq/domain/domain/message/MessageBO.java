package cn.coderule.minimq.domain.domain.message;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.collection.MapUtil;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.enums.message.MessageStatus;
import cn.coderule.minimq.domain.core.enums.message.MessageVersion;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import java.io.Serializable;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    @Builder.Default
    private int messageLength = -1;
    @Builder.Default
    private int topicLength = -1;
    @Builder.Default
    private int bodyLength = -1;
    @Builder.Default
    private int propertyLength = -1;
    @Builder.Default
    private boolean appendPropertyCRC = false;


    private long commitOffset;
    /**
     * set by consumeQueue while putting process of CommitLog
     */
    private long queueOffset;

    private int sysFlag;
    private long bornTimestamp;
    private SocketAddress bornHost;

    private long storeTimestamp;
    private SocketAddress storeHost;
    private String messageId;

    private int bodyCRC = -1;
    private int magicCode;

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

    private MessageVersion version = null;

    public static MessageBO notFound() {
        MessageBO messageBO = new MessageBO();
        messageBO.status = MessageStatus.NO_MATCHED_MESSAGE;
        return messageBO;
    }

    public MessageVersion getVersion() {
        if (null != this.version) {
            return this.version;
        }

        if (topic.length() > Byte.MAX_VALUE) {
            this.version = MessageVersion.V2;
        } else {
            this.version = MessageVersion.V1;
        }

        return this.version;
    }

    public int getBodyCRC() {
        if (this.bodyCRC > -1) {
            return this.bodyCRC;
        }

        if (null == body) {
            this.bodyCRC = 0;
        } else {
            this.bodyCRC = HashUtil.crc32(body);
        }

        return this.bodyCRC;
    }

    public void setSystemQueue(String systemTopic, int systemQueueId) {
        putProperty(MessageConst.PROPERTY_REAL_TOPIC, topic);
        putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(queueId));

        this.topic = systemTopic;
        this.queueId = systemQueueId;
    }

    public boolean isNormalOrCommitMessage(){
        int type = MessageSysFlag.getMessageType(this.sysFlag);
        return MessageSysFlag.NORMAL_MESSAGE == type
            || MessageSysFlag.COMMIT_MESSAGE == type;
    }

    public String getBodyString() {
        if (body == null) {
            return "";
        }

        return new String(body);
    }

    public byte[] getBornHostBytes() {
        if (null == this.bornHost) {
            return null;
        }

        return getBornHostBuffer().array();
    }

    public ByteBuffer getBornHostBuffer() {
        if (null == this.bornHost) {
            return null;
        }

        return MessageUtils.socketAddress2ByteBuffer(this.bornHost);
    }

    public byte[] getStoreHostBytes() {
        if (null == this.storeHost) {
            return null;
        }

        return getStoreHostBuffer().array();
    }

    public ByteBuffer getStoreHostBuffer() {
        if (null == this.storeHost) {
            return null;
        }

        return MessageUtils.socketAddress2ByteBuffer(this.storeHost);
    }

    public boolean isValid() {
        return MessageStatus.FOUND.equals(status);
    }

    public boolean isEmpty() {
        return this.messageLength <= 0;
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

    public String getUniqueKey() {
        return this.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX);
    }

    public void setUniqueKey(String tags) {
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
        return getLongProperty(MessageConst.PROPERTY_TIMER_DELIVER_MS);
    }

    public void setDelayTime(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_DELAY_MS, String.valueOf(timeMs));
    }

    public long getDelayTime() {
        return getLongProperty(MessageConst.PROPERTY_TIMER_DELAY_MS);
    }

    public long getDelaySecond() {
        return getLongProperty(MessageConst.PROPERTY_TIMER_DELAY_SEC);
    }

    public void setTimeout(long timeMs) {
        this.putProperty(MessageConst.PROPERTY_TIMER_OUT_MS, String.valueOf(timeMs));
    }

    public long getTimeout() {
        return getLongProperty(MessageConst.PROPERTY_TIMER_OUT_MS);
    }

    public long getTransactionCheckTime() {
        return getLongProperty(MessageConst.PROPERTY_CHECK_IMMUNITY_TIME_IN_SECONDS);
    }

    public String getReceipt() {
        return this.getProperty(MessageConst.PROPERTY_POP_CK);
    }


}
