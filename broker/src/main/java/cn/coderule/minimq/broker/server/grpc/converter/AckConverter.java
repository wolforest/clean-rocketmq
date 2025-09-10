package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.AckMessageEntry;
import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResultEntry;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.minimq.domain.domain.consumer.receipt.ReceiptHandle;
import cn.coderule.minimq.rpc.common.grpc.response.ResponseBuilder;

public class AckConverter {
    public static AckRequest toAckRequest(
        RequestContext context,
        AckMessageRequest request,
        AckMessageEntry entry
    ) {
        String requestTopic = request.getTopic().getName();
        String requestGroup = request.getGroup().getName();

        AckRequest ackRequest = AckRequest.builder()
            .requestContext(context)
            .groupName(requestGroup)
            .build();


        ReceiptHandle handle = ReceiptHandle.decode(entry.getReceiptHandle());
        ackRequest.addReceipt(entry.getMessageId(), handle);

        String realTopic = handle.getRealTopic(requestTopic, requestGroup);
        ackRequest.setTopicName(realTopic);
        ackRequest.setQueueId(handle.getQueueId());
        ackRequest.setOffset(handle.getOffset());

        return ackRequest;
    }

    public static AckMessageResultEntry toResultEntry(
        AckMessageEntry entry,
        AckResult ackResult
    ) {

        Status status;
        if (ackResult.isSuccess()) {
            status = ResponseBuilder.getInstance()
                .buildStatus(Code.OK, Code.OK.name());
        } else {
            status = ResponseBuilder.getInstance()
                .buildStatus(Code.INTERNAL_SERVER_ERROR, "ack failed");
        }

        return AckMessageResultEntry.newBuilder()
            .setMessageId(entry.getMessageId())
            .setReceiptHandle(entry.getReceiptHandle())
            .setStatus(status)
            .build();
    }

    public static AckMessageResultEntry toResultEntry(
        AckMessageEntry entry,
        Throwable t
    ) {
        Status status = ResponseBuilder.getInstance()
            .buildStatus(t);

        return AckMessageResultEntry.newBuilder()
            .setMessageId(entry.getMessageId())
            .setReceiptHandle(entry.getReceiptHandle())
            .setStatus(status)
            .build();
    }
}
