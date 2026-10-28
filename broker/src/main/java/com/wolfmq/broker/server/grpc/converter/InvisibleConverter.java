package com.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Status;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import com.wolfmq.rpc.common.grpc.response.ResponseBuilder;
import com.google.protobuf.util.Durations;

public class InvisibleConverter {
    public static InvisibleRequest toInvisibleRequest(
        RequestContext context,
        ChangeInvisibleDurationRequest request
    ) {
        long invisibleTime = Durations.toMillis(request.getInvisibleDuration());
        return InvisibleRequest.builder()
            .requestContext(context)
            .receiptStr(request.getReceiptHandle())
            .messageId(request.getMessageId())
            .topicName(request.getTopic().getName())
            .groupName(request.getGroup().getName())
            .invisibleTime(invisibleTime)
            .build();
    }

    public static ChangeInvisibleDurationResponse toResponse(AckResult ackResult) {
        Status status;
        if (ackResult.isSuccess()) {
            status = ResponseBuilder.getInstance()
                .buildStatus(Code.OK, Code.OK.name());
        } else {
            status = ResponseBuilder.getInstance()
                .buildStatus(Code.INTERNAL_SERVER_ERROR, "");
        }

        return ChangeInvisibleDurationResponse.newBuilder()
            .setStatus(status)
            .setReceiptHandle(ackResult.getReceiptStr())
            .build();
    }


}
