package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.AckMessageEntry;
import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResultEntry;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;

public class AckConverter {
    public static AckRequest toAckRequest(
        RequestContext context,
        AckMessageRequest request,
        AckMessageEntry entry
    ) {

        AckRequest ackRequest = AckRequest.builder()
            .requestContext(context)
            .topicName(request.getTopic().getName())
            .groupName(request.getGroup().getName())
            .build();

        ReceiptHandle handle = ReceiptHandle.decode(entry.getReceiptHandle());
        ackRequest.addReceipt(entry.getMessageId(), handle);

        return ackRequest;
    }

    public static AckMessageResultEntry toResultEntry(
        RequestContext context,
        AckMessageEntry entry,
        AckResult ackResult
    ) {
        return null;
    }

    public static AckMessageResultEntry toResultEntry(
        RequestContext context,
        AckMessageEntry entry,
        Throwable t
    ) {
        return null;
    }
}
