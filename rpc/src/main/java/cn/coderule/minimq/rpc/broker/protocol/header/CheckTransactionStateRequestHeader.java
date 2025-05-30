

/**
 * $Id: EndTransactionRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.broker.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import com.google.common.base.MoreObjects;


@RocketMQAction(value = RequestCode.CHECK_TRANSACTION_STATE, action = Action.PUB)
public class CheckTransactionStateRequestHeader extends RpcRequestHeader {
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;
    @CFNotNull
    private Long tranStateTableOffset;
    @CFNotNull
    private Long commitLogOffset;
    private String msgId;
    private String transactionId;
    private String offsetMsgId;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Long getTranStateTableOffset() {
        return tranStateTableOffset;
    }

    public void setTranStateTableOffset(Long tranStateTableOffset) {
        this.tranStateTableOffset = tranStateTableOffset;
    }

    public Long getCommitLogOffset() {
        return commitLogOffset;
    }

    public void setCommitLogOffset(Long commitLogOffset) {
        this.commitLogOffset = commitLogOffset;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOffsetMsgId() {
        return offsetMsgId;
    }

    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tranStateTableOffset", tranStateTableOffset)
            .add("commitLogOffset", commitLogOffset)
            .add("msgId", msgId)
            .add("transactionId", transactionId)
            .add("offsetMsgId", offsetMsgId)
            .toString();
    }
}
