package cn.coderule.minimq.broker.server.grpc.converter;

import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import cn.coderule.minimq.domain.domain.cluster.RequestContext;
import cn.coderule.minimq.domain.domain.consumer.ack.broker.InvisibleRequest;
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
}
