package cn.coderule.wolfmq.broker.server.grpc.converter;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.SendMessageResponse;
import cn.coderule.wolfmq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProducerConverterTest {

    @Test
    void testToSendMessageResponseSuccess() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId("msg123")
            .transactionId("trans456")
            .commitOffset(1000)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals(Code.OK, response.getStatus().getCode());
        assertEquals(1, response.getEntriesCount());
        assertEquals("msg123", response.getEntries(0).getMessageId());
        assertEquals("trans456", response.getEntries(0).getTransactionId());
        assertEquals(1000, response.getEntries(0).getOffset());
    }

    @Test
    void testToSendMessageResponseFlushDiskTimeout() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.FLUSH_DISK_TIMEOUT)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals(Code.MASTER_PERSISTENCE_TIMEOUT, response.getStatus().getCode());
    }

    @Test
    void testToSendMessageResponseFlushSlaveTimeout() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.FLUSH_SLAVE_TIMEOUT)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals(Code.SLAVE_PERSISTENCE_TIMEOUT, response.getStatus().getCode());
    }

    @Test
    void testToSendMessageResponseSlaveNotAvailable() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.SLAVE_NOT_AVAILABLE)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals(Code.HA_NOT_AVAILABLE, response.getStatus().getCode());
    }

    @Test
    void testToSendMessageResponseUnknownError() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.UNKNOWN_ERROR)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals(Code.INTERNAL_SERVER_ERROR, response.getStatus().getCode());
    }

    @Test
    void testToSendMessageResponseMultipleResults() {
        EnqueueResult result1 = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .build();
        EnqueueResult result2 = EnqueueResult.builder()
            .status(EnqueueStatus.FLUSH_DISK_TIMEOUT)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(List.of(result1, result2));

        assertNotNull(response);
        assertEquals(Code.MULTIPLE_RESULTS, response.getStatus().getCode());
        assertEquals(2, response.getEntriesCount());
    }

    @Test
    void testToSendMessageResponseEmptyList() {
        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.emptyList());

        assertNotNull(response);
        assertEquals(Code.INTERNAL_SERVER_ERROR, response.getStatus().getCode());
        assertEquals(0, response.getEntriesCount());
    }

    @Test
    void testToSendMessageResponseNullMessageId() {
        EnqueueResult result = EnqueueResult.builder()
            .status(EnqueueStatus.PUT_OK)
            .messageId(null)
            .transactionId(null)
            .build();

        SendMessageResponse response = ProducerConverter.toSendMessageResponse(Collections.singletonList(result));

        assertNotNull(response);
        assertEquals("", response.getEntries(0).getMessageId());
        assertEquals("", response.getEntries(0).getTransactionId());
    }
}
