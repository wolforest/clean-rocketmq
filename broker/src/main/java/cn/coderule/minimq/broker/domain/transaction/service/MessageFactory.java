package cn.coderule.minimq.broker.domain.transaction.service;

import cn.coderule.minimq.domain.config.server.BrokerConfig;
import cn.coderule.minimq.domain.domain.MessageQueue;
import cn.coderule.minimq.domain.domain.message.MessageIDSetter;
import cn.coderule.minimq.domain.domain.transaction.SubmitRequest;
import cn.coderule.minimq.domain.domain.transaction.TransactionUtil;
import cn.coderule.minimq.domain.domain.transaction.CommitBuffer;
import cn.coderule.minimq.domain.domain.transaction.OffsetQueue;
import cn.coderule.minimq.domain.config.business.TransactionConfig;
import cn.coderule.minimq.domain.core.constant.MessageConst;
import cn.coderule.minimq.domain.core.constant.flag.MessageSysFlag;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

public class MessageFactory {
    private final BrokerConfig brokerConfig;
    private final TransactionConfig transactionConfig;
    private final SocketAddress host;

    public MessageFactory(BrokerConfig brokerConfig, CommitBuffer commitBuffer) {
        this.brokerConfig = brokerConfig;
        this.transactionConfig = brokerConfig.getTransactionConfig();
        this.host = brokerConfig.getHostAddress();
    }

    public MessageBO createPrepareMessage(MessageBO msg) {
        String uniqId = msg.getUniqueKey();
        if (uniqId != null && !uniqId.isEmpty()) {
            msg.setTransactionId(uniqId);
        } else {
            uniqId = MessageIDSetter.createUniqID();
            msg.setUniqueKey(uniqId);
            msg.setTransactionId(uniqId);
        }

        msg.putProperty(MessageConst.PROPERTY_REAL_TOPIC, msg.getTopic());
        msg.putProperty(MessageConst.PROPERTY_REAL_QUEUE_ID, String.valueOf(msg.getQueueId()));

        //reset msg transaction type and set topic = TopicValidator.RMQ_SYS_TRANS_HALF_TOPIC
        msg.setSysFlag(MessageSysFlag.resetTransactionType(msg.getSysFlag(), MessageSysFlag.NORMAL_MESSAGE));
        msg.setTopic(TransactionUtil.buildPrepareTopic());
        msg.setQueueId(0);
        msg.initPropertiesString();
        return msg;
    }

    public MessageBO createCommitMessage(SubmitRequest request, MessageBO prepareMessage) {
        MessageBO newMsg = new MessageBO();
        newMsg.setWaitStore(false);

        newMsg.setTransactionId(prepareMessage.getProperty(MessageConst.PROPERTY_UNIQ_CLIENT_MESSAGE_ID_KEYIDX));
        newMsg.setBody(prepareMessage.getBody());

        newMsg.setTopic(prepareMessage.getProperty(MessageConst.PROPERTY_REAL_TOPIC));
        newMsg.setQueueId(prepareMessage.getIntProperty(MessageConst.PROPERTY_REAL_QUEUE_ID));

        newMsg.setFlag(prepareMessage.getFlag());
        newMsg.setTagsCode(prepareMessage.getTagsCode());

        newMsg.setBornHost(prepareMessage.getBornHost());
        newMsg.setBornTimestamp(prepareMessage.getBornTimestamp());
        newMsg.setStoreHost(prepareMessage.getStoreHost());
        newMsg.setStoreTimestamp(prepareMessage.getStoreTimestamp());

        newMsg.setReconsumeTime(prepareMessage.getReconsumeTime());

        newMsg.setProperties(prepareMessage.getProperties());
        newMsg.removeProperty(MessageConst.PROPERTY_REAL_TOPIC);
        newMsg.removeProperty(MessageConst.PROPERTY_REAL_QUEUE_ID);
        newMsg.removeProperty(MessageConst.PROPERTY_TRANSACTION_PREPARED);

        newMsg.setQueueOffset(request.getQueueOffset());
        newMsg.setPrepareOffset(request.getCommitOffset());

        int sysFlag = MessageSysFlag.resetTransactionType(prepareMessage.getSysFlag(), request.getTransactionFlag());
        newMsg.setSysFlag(sysFlag);

        return newMsg;
    }

    public MessageBO createOperationMessage(OffsetQueue offsetQueue, MessageQueue operationQueue) {
        return createOperationMessage(offsetQueue, operationQueue, -1);
    }

    public MessageBO createOperationMessage(OffsetQueue offsetQueue, MessageQueue operationQueue, long queueOffset) {
        String offsetKey = queueOffset >= 0
            ? TransactionUtil.buildOffsetKey(queueOffset)
            : null;

        int offsetLength = null != offsetKey
            ? offsetKey.length()
            : 0;

        int bodyLength = calculateBodyLength(offsetLength, offsetQueue);
        StringBuilder bodyBuilder = buildBody(bodyLength, offsetKey, offsetQueue);
        if (bodyBuilder.isEmpty()) {
            return null;
        }

        int size = bodyBuilder.length() - offsetLength;
        offsetQueue.addAndGet(size);

        return createOperationMessage(bodyBuilder, operationQueue);
    }

    private MessageBO createOperationMessage(StringBuilder bodyBuilder, MessageQueue operationQueue) {
        byte[] body = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
        String commitTopic = TransactionUtil.buildOperationTopic();
        long now = System.currentTimeMillis();

        MessageBO messageBO = MessageBO.builder()
            .topic(commitTopic)
            .body(body)
            .queueId(operationQueue.getQueueId())
            .sysFlag(0)
            .bornTimestamp(now)
            .storeTimestamp(now)
            .bornHost(host)
            .storeHost(host)
            .build();

        messageBO.setWaitStore(false);
        messageBO.setTags(TransactionUtil.REMOVE_TAG);
        MessageIDSetter.setUniqID(messageBO);

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
