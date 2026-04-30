package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.AckMessageEntry;
import apache.rocketmq.v2.AckMessageRequest;
import apache.rocketmq.v2.AckMessageResultEntry;
import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Resource;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckRequest;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.AckResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AckConverterTest {

    @Test
    void testToAckRequest() {
        RequestContext context = RequestContext.create("testGroup");
        AckMessageRequest request = AckMessageRequest.newBuilder()
            .setTopic(Resource.newBuilder().setName("TestTopic").build())
            .setGroup(Resource.newBuilder().setName("TestGroup").build())
            .build();
        AckMessageEntry entry = AckMessageEntry.newBuilder()
            .setMessageId("msg123")
            .setReceiptHandle("receipt456")
            .build();

        AckRequest ackRequest = AckConverter.toAckRequest(context, request, entry);

        assertNotNull(ackRequest);
        assertEquals(context, ackRequest.getRequestContext());
        assertEquals("TestTopic", ackRequest.getTopicName());
        assertEquals("TestGroup", ackRequest.getGroupName());
        assertEquals("msg123", ackRequest.getMessageId());
        assertEquals("receipt456", ackRequest.getReceiptStr());
    }

    @Test
    void testToResultEntrySuccess() {
        AckMessageEntry entry = AckMessageEntry.newBuilder()
            .setMessageId("msg123")
            .setReceiptHandle("receipt456")
            .build();
        AckResult ackResult = AckResult.success();

        AckMessageResultEntry resultEntry = AckConverter.toResultEntry(entry, ackResult);

        assertNotNull(resultEntry);
        assertEquals("msg123", resultEntry.getMessageId());
        assertEquals("receipt456", resultEntry.getReceiptHandle());
        assertEquals(Code.OK, resultEntry.getStatus().getCode());
    }

    @Test
    void testToResultEntryFailure() {
        AckMessageEntry entry = AckMessageEntry.newBuilder()
            .setMessageId("msg123")
            .setReceiptHandle("receipt456")
            .build();
        AckResult ackResult = AckResult.failure();

        AckMessageResultEntry resultEntry = AckConverter.toResultEntry(entry, ackResult);

        assertNotNull(resultEntry);
        assertEquals("msg123", resultEntry.getMessageId());
        assertEquals("receipt456", resultEntry.getReceiptHandle());
        assertEquals(Code.INTERNAL_SERVER_ERROR, resultEntry.getStatus().getCode());
    }

    @Test
    void testToResultEntryWithThrowable() {
        AckMessageEntry entry = AckMessageEntry.newBuilder()
            .setMessageId("msg123")
            .setReceiptHandle("receipt456")
            .build();
        RuntimeException exception = new RuntimeException("test error");

        AckMessageResultEntry resultEntry = AckConverter.toResultEntry(entry, exception);

        assertNotNull(resultEntry);
        assertEquals("msg123", resultEntry.getMessageId());
        assertEquals("receipt456", resultEntry.getReceiptHandle());
        assertEquals(Code.INTERNAL_SERVER_ERROR, resultEntry.getStatus().getCode());
    }
}
