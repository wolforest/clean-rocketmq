package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import apache.rocketmq.v2.Code;
import apache.rocketmq.v2.Message;
import apache.rocketmq.v2.ReceiveMessageResponse;
import apache.rocketmq.v2.Resource;
import apache.rocketmq.v2.SystemProperties;
import cn.coderule.wolfmq.broker.api.ConsumerController;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.core.enums.consume.PopStatus;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest;
import cn.coderule.wolfmq.domain.domain.consumer.consume.pop.PopResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.rpc.common.grpc.channel.GrpcConverter;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ConsumeResponseWriterTest {

    @Mock
    private ConsumerController consumerController;

    @Mock
    private StreamObserver<ReceiveMessageResponse> streamObserver;

    private ConsumeResponseWriter writer;

    private AutoCloseable mocks;
    private GrpcConverter originalGrpcConverter;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        originalGrpcConverter = GrpcConverter.getInstance();

        GrpcConverter mockGrpcConverter = mock(GrpcConverter.class);
        when(mockGrpcConverter.buildMessage(any(MessageBO.class))).thenReturn(createMockMessage());
        setGrpcConverterInstance(mockGrpcConverter);

        when(consumerController.changeInvisible(any(InvisibleRequest.class)))
            .thenReturn(CompletableFuture.completedFuture(null));

        writer = new ConsumeResponseWriter(consumerController, streamObserver);
    }

    @AfterEach
    void tearDown() throws Exception {
        setGrpcConverterInstance(originalGrpcConverter);
        mocks.close();
    }

    private void setGrpcConverterInstance(GrpcConverter instance) throws Exception {
        Field instanceField = GrpcConverter.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, instance);
    }

    private Message createMockMessage() {
        return Message.newBuilder()
            .setTopic(Resource.newBuilder().setName("testTopic").build())
            .setSystemProperties(SystemProperties.newBuilder()
                .setBornTimestamp(Timestamps.fromMillis(0))
                .setStoreTimestamp(Timestamps.fromMillis(0))
                .setQueueId(0)
                .setQueueOffset(0)
                .setDeliveryAttempt(1)
                .build())
            .setBody(ByteString.EMPTY)
            .build();
    }

    private MessageBO createMessageBO(String topic, String uniqueKey, String popCk) {
        MessageBO messageBO = MessageBO.builder()
            .topic(topic)
            .body(new byte[0])
            .bornTimestamp(System.currentTimeMillis())
            .storeTimestamp(System.currentTimeMillis())
            .queueId(0)
            .queueOffset(0L)
            .reconsumeTimes(0)
            .sysFlag(0)
            .build();
        if (uniqueKey != null) {
            messageBO.setUniqueKey(uniqueKey);
        }
        if (popCk != null) {
            messageBO.putProperty(MessageConst.PROPERTY_POP_CK, popCk);
        }
        return messageBO;
    }

    @Test
    @DisplayName("noSettings should send INTERNAL_ERROR response and complete stream")
    void noSettings() {
        writer.noSettings();

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.INTERNAL_ERROR, responses.get(0).getStatus().getCode());
        assertEquals("Settings of grpc can not be null", responses.get(0).getStatus().getMessage());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("notEnoughTime should send ILLEGAL_POLLING_TIME response and complete stream")
    void notEnoughTime() {
        writer.notEnoughTime();

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.ILLEGAL_POLLING_TIME, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("illegalFilter should send ILLEGAL_FILTER_EXPRESSION response and complete stream")
    void illegalFilter() {
        writer.illegalFilter();

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.ILLEGAL_FILTER_EXPRESSION, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with FOUND status and messages should send OK status, then messages, then deliveryTimestamp")
    void writeWithFoundStatusAndMessages() {
        MessageBO msg1 = createMessageBO("topic1", "key1", null);
        MessageBO msg2 = createMessageBO("topic2", "key2", null);

        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.FOUND)
            .messageList(List.of(msg1, msg2))
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(4)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();

        // First response: OK status
        assertEquals(Code.OK, responses.get(0).getStatus().getCode());
        assertFalse(responses.get(0).hasMessage());

        // Second and third responses: messages
        assertTrue(responses.get(1).hasMessage());
        assertTrue(responses.get(2).hasMessage());

        // Last response: deliveryTimestamp
        assertTrue(responses.get(3).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with FOUND status and empty result should send MESSAGE_NOT_FOUND")
    void writeWithFoundStatusEmptyResult() {
        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.FOUND)
            .messageList(new ArrayList<>())
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.MESSAGE_NOT_FOUND, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with POLLING_FULL status should send TOO_MANY_REQUESTS")
    void writeWithPollingFullStatus() {
        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.POLLING_FULL)
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.TOO_MANY_REQUESTS, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with NO_NEW_MSG status should send MESSAGE_NOT_FOUND")
    void writeWithNoNewMsgStatus() {
        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.NO_NEW_MSG)
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.MESSAGE_NOT_FOUND, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with POLLING_NOT_FOUND status should send MESSAGE_NOT_FOUND (default case)")
    void writeWithPollingNotFoundStatus() {
        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.POLLING_NOT_FOUND)
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.MESSAGE_NOT_FOUND, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with Code and message should send response with given code and message")
    void writeWithCodeAndMessage() {
        writer.write(RequestContext.create(), Code.BAD_REQUEST, "invalid request");

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.BAD_REQUEST, responses.get(0).getStatus().getCode());
        assertEquals("invalid request", responses.get(0).getStatus().getMessage());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with Throwable should send INTERNAL_SERVER_ERROR response")
    void writeWithThrowable() {
        writer.write(RequestContext.create(), new RuntimeException("test error"));

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(2)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.INTERNAL_SERVER_ERROR, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasDeliveryTimestamp());
    }

    @Test
    @DisplayName("write with FOUND status and stream failure should call changeInvisible for remaining messages only")
    void writeWithStreamFailure() throws Exception {
        // Set up GrpcConverter mock to return a mock message
        GrpcConverter mockGrpcConverter = mock(GrpcConverter.class);
        when(mockGrpcConverter.buildMessage(any(MessageBO.class))).thenReturn(createMockMessage());
        setGrpcConverterInstance(mockGrpcConverter);

        // Create messages with PROPERTY_POP_CK so changeInvisible will be called
        MessageBO msg1 = createMessageBO("topic1", "key1", "handle1");
        MessageBO msg2 = createMessageBO("topic2", "key2", "handle2");
        MessageBO msg3 = createMessageBO("topic3", "key3", "handle3");

        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.FOUND)
            .messageList(List.of(msg1, msg2, msg3))
            .build();

        // Make streamObserver.onNext() throw on the 3rd call
        // Call 1: OK status (writeOkStatus)
        // Call 2: message 1 (writeMessage)
        // Call 3: message 2 (writeMessage) - THROW HERE
        AtomicInteger onNextCounter = new AtomicInteger(0);
        doAnswer(invocation -> {
            int count = onNextCounter.incrementAndGet();
            if (count == 3) {
                throw new RuntimeException("stream error");
            }
            return null;
        }).when(streamObserver).onNext(any(ReceiveMessageResponse.class));

        writer.write(RequestContext.create(), popResult);

        // Verify changeInvisible called for msg2 (the failed one) and msg3 (remaining)
        // but NOT for msg1 (the successfully sent one)
        ArgumentCaptor<InvisibleRequest> invisibleCaptor = ArgumentCaptor.forClass(InvisibleRequest.class);
        verify(consumerController, times(2)).changeInvisible(invisibleCaptor.capture());

        List<InvisibleRequest> invisibleRequests = invisibleCaptor.getAllValues();
        assertEquals("key2", invisibleRequests.get(0).getMessageId());
        assertEquals("topic2", invisibleRequests.get(0).getTopicName());
        assertEquals("key3", invisibleRequests.get(1).getMessageId());
        assertEquals("topic3", invisibleRequests.get(1).getTopicName());

        // Verify onCompleted was still called
        verify(streamObserver).onCompleted();
    }

    @Test
    @DisplayName("write with FOUND status and stream failure should not call changeInvisible for messages without POP_CK")
    void writeWithStreamFailureNoPopCk() throws Exception {
        GrpcConverter mockGrpcConverter = mock(GrpcConverter.class);
        when(mockGrpcConverter.buildMessage(any(MessageBO.class))).thenReturn(createMockMessage());
        setGrpcConverterInstance(mockGrpcConverter);

        // msg2 has no POP_CK, msg3 has POP_CK
        MessageBO msg1 = createMessageBO("topic1", "key1", "handle1");
        MessageBO msg2 = createMessageBO("topic2", "key2", null);
        MessageBO msg3 = createMessageBO("topic3", "key3", "handle3");

        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.FOUND)
            .messageList(List.of(msg1, msg2, msg3))
            .build();

        AtomicInteger onNextCounter = new AtomicInteger(0);
        doAnswer(invocation -> {
            int count = onNextCounter.incrementAndGet();
            if (count == 3) {
                throw new RuntimeException("stream error");
            }
            return null;
        }).when(streamObserver).onNext(any(ReceiveMessageResponse.class));

        writer.write(RequestContext.create(), popResult);

        // changeInvisible should be called for msg2 (but it has no POP_CK, so consumerController is NOT called)
        // and for msg3 (has POP_CK, so consumerController IS called)
        // msg2: changeInvisible is called internally but returns early because POP_CK is null
        // msg3: changeInvisible calls consumerController.changeInvisible()
        ArgumentCaptor<InvisibleRequest> invisibleCaptor = ArgumentCaptor.forClass(InvisibleRequest.class);
        verify(consumerController, times(1)).changeInvisible(invisibleCaptor.capture());

        InvisibleRequest request = invisibleCaptor.getValue();
        assertEquals("key3", request.getMessageId());
        assertEquals("topic3", request.getTopicName());

        verify(streamObserver).onCompleted();
    }

    @Test
    @DisplayName("write with FOUND status and single message should send OK status, one message, then deliveryTimestamp")
    void writeWithFoundStatusSingleMessage() {
        MessageBO msg = createMessageBO("topic1", "key1", null);

        PopResult popResult = PopResult.builder()
            .popStatus(PopStatus.FOUND)
            .messageList(List.of(msg))
            .build();

        writer.write(RequestContext.create(), popResult);

        ArgumentCaptor<ReceiveMessageResponse> captor = ArgumentCaptor.forClass(ReceiveMessageResponse.class);
        verify(streamObserver, times(3)).onNext(captor.capture());
        verify(streamObserver).onCompleted();

        List<ReceiveMessageResponse> responses = captor.getAllValues();
        assertEquals(Code.OK, responses.get(0).getStatus().getCode());
        assertTrue(responses.get(1).hasMessage());
        assertTrue(responses.get(2).hasDeliveryTimestamp());
    }
}