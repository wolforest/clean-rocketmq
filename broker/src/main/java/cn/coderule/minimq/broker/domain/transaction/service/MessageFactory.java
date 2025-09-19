package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.OffsetQueue;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.utils.message.MessageUtils;
import java.nio.charset.StandardCharsets;

public class MessageFactory {
    private final TransactionConfig transactionConfig;
    private final CommitBuffer commitBuffer;

    public MessageFactory(TransactionConfig transactionConfig, CommitBuffer commitBuffer) {
        this.transactionConfig = transactionConfig;
        this.commitBuffer = commitBuffer;
    }

    public MessageBO createPrepareMessage(MessageBO msg) {
        String uniqId = msg.getUniqueKey();
        if (uniqId != null && !uniqId.isEmpty()) {
            msg.putProperty(TransactionUtil.TRANSACTION_ID, uniqId);
        }

        msg.putProperty(MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
        msg.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));

        //reset msg transaction type and set topic = TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC
        msg.setSysFlag(MessageSysFlag.resetTransactionType(msg.getSysFlag(), MessageSysFlag.NORMAL_MESSAGE));
        msg.setTopic(TransactionUtil.buildPrepareTopic());
        msg.setQueueId(0);
        msg.setPropertiesString(MessageUtils.propertiesToString(msg.getProperties()));
        return msg;
    }

    public MessageBO recreatePrepareMessage(MessageBO prepareMessage) {
        MessageBO newMsg = new MessageBO();
        newMsg.setWaitStore(false);
        newMsg.setMessageId(prepareMessage.getMessageId());
        newMsg.setTopic(prepareMessage.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        newMsg.setBody(prepareMessage.getBody());
        String realQueueIdStr = prepareMessage.getProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        if (StringUtil.isNumeric(realQueueIdStr)) {
            newMsg.setQueueId(Integer.parseInt(realQueueIdStr));
        }
        newMsg.setFlag(prepareMessage.getFlag());
        newMsg.setTagsCode(prepareMessage.getTagsCode());
        newMsg.setBornTimestamp(prepareMessage.getBornTimestamp());
        newMsg.setBornHost(prepareMessage.getBornHost());
        newMsg.setTransactionId(prepareMessage.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));

        newMsg.setProperties(prepareMessage.getProperties());
        newMsg.putProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED, "true");
        newMsg.removeProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED_QUEUE_OFFSET);
        newMsg.removeProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);

        newMsg.setPropertiesString(MessageUtils.propertiesToString(newMsg.getProperties()));

        int sysFlag = prepareMessage.getSysFlag();
        sysFlag |= MessageSysFlag.PREPARE_MESSAGE;
        newMsg.setSysFlag(sysFlag);

        return newMsg;
    }

    public MessageBO createCommitMessage(int queueId, String offset) {
        OffsetQueue offsetQueue = commitBuffer.getQueue(queueId);

        int offsetLength = null == offset ? 0 : offset.length();

        int bodyLength = calculateBodyLength(offsetLength, offsetQueue);
        StringBuilder bodyBuilder = buildBody(bodyLength, offset, offsetQueue);
        if (bodyBuilder.isEmpty()) {
            return null;
        }

        int size = bodyBuilder.length() - offsetLength;
        offsetQueue.addAndGet(size);

        return buildCommitMessage(bodyBuilder);
    }

    private MessageBO buildCommitMessage(StringBuilder bodyBuilder) {
        byte[] body = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
        String commitTopic = TransactionUtil.buildCommitTopic();
        MessageBO messageBO = MessageBO.builder()
            .topic(commitTopic)
            .body(body)
            .build();

        messageBO.setTags(TransactionUtil.REMOVE_TAG);

        return messageBO;
    }

    private StringBuilder buildBody(int bodyLength, String offset, OffsetQueue offsetQueue) {
        StringBuilder sb = new StringBuilder(bodyLength);
        if (offset != null) {
            sb.append(offset);
        }

        int maxLength = transactionConfig.getMaxCommitMessageLength();
        while (!offsetQueue.isEmpty()) {
            if (sb.length() >= maxLength) {
                break;
            }

            String data = offsetQueue.poll();
            if (data != null) {
                sb.append(data);
            }
        }

        return sb;
    }

    private int calculateBodyLength(int offsetLength, OffsetQueue offsetQueue) {
        int bodyLength = offsetLength;
        int maxLength = transactionConfig.getMaxCommitMessageLength();

        if (bodyLength >= maxLength) {
            return bodyLength;
        }

        int size = offsetQueue.getTotalSize();
        if (size > maxLength || bodyLength + size > maxLength) {
            bodyLength = maxLength + 100;
        } else {
            bodyLength += size;
        }

        return bodyLength;
    }
}
