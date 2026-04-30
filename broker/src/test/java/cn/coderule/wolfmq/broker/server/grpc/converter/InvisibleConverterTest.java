package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.ChangeInvisibleDurationRequest;
import apache.rocketmq.v2.ChangeInvisibleDurationResponse;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Resource;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import com.google.protobuf.Duration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvisibleConverterTest {

    @Test
    void testToInvisibleRequest() {
        RequestContext context = RequestContext.create("testGroup");
        ChangeInvisibleDurationRequest request = ChangeInvisibleDurationRequest.newBuilder()
            .setTopic(Resource.newBuilder().setName("TestTopic").build())
            .setGroup(Resource.newBuilder().setName("TestGroup").build())
            .setMessageId("msg123")
            .setReceiptHandle("receipt456")
            .setInvisibleDuration(Duration.newBuilder().setSeconds(30).build())
            .build();

        InvisibleRequest invisibleRequest = InvisibleConverter.toInvisibleRequest(context, request);

        assertNotNull(invisibleRequest);
        assertEquals(context, invisibleRequest.getRequestContext());
        assertEquals("TestTopic", invisibleRequest.getTopicName());
        assertEquals("TestGroup", invisibleRequest.getGroupName());
        assertEquals("msg123", invisibleRequest.getMessageId());
        assertEquals("receipt456", invisibleRequest.getReceiptStr());
        assertEquals(30000, invisibleRequest.getInvisibleTime()); // 30 seconds in millis
    }

    @Test
    void testToResponseSuccess() {
        AckResult ackResult = AckResult.success();
        ackResult.setReceiptStr("newReceipt123");

        ChangeInvisibleDurationResponse response = InvisibleConverter.toResponse(ackResult);

        assertNotNull(response);
        assertEquals(Code.OK, response.getStatus().getCode());
        assertEquals("newReceipt123", response.getReceiptHandle());
    }

    @Test
    void testToResponseFailure() {
        AckResult ackResult = AckResult.failure();

        ChangeInvisibleDurationResponse response = InvisibleConverter.toResponse(ackResult);

        assertNotNull(response);
        assertEquals(Code.INTERNAL_SERVER_ERROR, response.getStatus().getCode());
    }
}
