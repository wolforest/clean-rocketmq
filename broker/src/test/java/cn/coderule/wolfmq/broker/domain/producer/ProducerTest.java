package cn.coderule.wolfmq.broker.domain.producer;

import cn.coderule.wolfmq.domain.domain.cluster.ClientChannelInfo;
import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.store.domain.mq.EnqueueResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProducerTest {

    @Mock
    private EnqueueService enqueueService;

    @Mock
    private ProducerManager producerManager;

    private Producer producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new Producer(enqueueService, producerManager);
    }

    @Test
    void testConstructor() {
        assertNotNull(producer);
    }

    @Test
    void testRegister() {
        RequestContext context = RequestContext.create("testGroup");
        String groupName = "testGroup";
        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .clientId("client1")
            .build();

        producer.register(context, groupName, channelInfo);

        verify(producerManager).register(groupName, channelInfo);
    }

    @Test
    void testUnregister() {
        RequestContext context = RequestContext.create("testGroup");
        String groupName = "testGroup";
        ClientChannelInfo channelInfo = ClientChannelInfo.builder()
            .clientId("client1")
            .build();

        producer.unregister(context, groupName, channelInfo);

        verify(producerManager).unregister(groupName, channelInfo);
    }

    @Test
    void testScanIdleChannels() {
        producer.scanIdleChannels();
        verify(producerManager).scanIdleChannels();
    }

    @Test
    void testProduceSingleMessage() {
        RequestContext context = RequestContext.create("testGroup");
        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .body("test".getBytes())
            .build();

        EnqueueResult expectedResult = EnqueueResult.builder().build();
        when(enqueueService.enqueue(context, message))
            .thenReturn(CompletableFuture.completedFuture(expectedResult));

        CompletableFuture<EnqueueResult> future = producer.produce(context, message);

        assertNotNull(future);
        verify(enqueueService).enqueue(context, message);
    }

    @Test
    void testProduceMultipleMessages() {
        RequestContext context = RequestContext.create("testGroup");
        List<MessageBO> messages = Arrays.asList(
            MessageBO.builder().topic("TestTopic").body("test1".getBytes()).build(),
            MessageBO.builder().topic("TestTopic").body("test2".getBytes()).build()
        );

        List<EnqueueResult> expectedResults = Arrays.asList(
            EnqueueResult.builder().build(),
            EnqueueResult.builder().build()
        );
        when(enqueueService.enqueue(context, messages))
            .thenReturn(CompletableFuture.completedFuture(expectedResults));

        CompletableFuture<List<EnqueueResult>> future = producer.produce(context, messages);

        assertNotNull(future);
        verify(enqueueService).enqueue(context, messages);
    }
}
