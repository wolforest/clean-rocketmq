package com.wolfmq.broker.domain.consumer;

import com.wolfmq.broker.domain.consumer.ack.BrokerAckService;
import com.wolfmq.broker.domain.consumer.ack.InvisibleService;
import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.broker.domain.consumer.pop.PopService;
import com.wolfmq.domain.server.RequestContext;
import com.wolfmq.domain.domain.consumer.ConsumerInfo;
import com.wolfmq.rpc.store.facade.SubscriptionFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsumerTest {

    @Mock
    private PopService popService;

    @Mock
    private BrokerAckService ackService;

    @Mock
    private ConsumerManager register;

    @Mock
    private InvisibleService invisibleService;

    @Mock
    private SubscriptionFacade subscriptionStore;

    private Consumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new Consumer(popService, ackService, register, invisibleService, subscriptionStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(consumer);
    }

    @Test
    void testRegister() {
        ConsumerInfo consumerInfo = ConsumerInfo.builder()
            .groupName("testGroup")
            .build();
        when(register.register(consumerInfo)).thenReturn(true);

        boolean result = consumer.register(consumerInfo);

        assertTrue(result);
        verify(register).register(consumerInfo);
    }

    @Test
    void testUnregister() {
        ConsumerInfo consumerInfo = ConsumerInfo.builder()
            .groupName("testGroup")
            .build();

        consumer.unregister(consumerInfo);

        verify(register).unregister(consumerInfo);
    }

    @Test
    void testScanIdleChannels() {
        consumer.scanIdleChannels();
        verify(register).scanIdleChannels();
    }

    @Test
    void testGetGroupInfo() {
        RequestContext context = RequestContext.create("testGroup");
        String groupName = "testGroup";

        consumer.getGroupInfo(context, groupName);

        verify(register).getGroupInfo(groupName);
    }

    @Test
    void testPopMessage() {
        com.wolfmq.domain.domain.consumer.consume.pop.PopRequest request =
            com.wolfmq.domain.domain.consumer.consume.pop.PopRequest.builder()
                .topicName("TestTopic")
                .build();

        consumer.popMessage(request);

        verify(popService).pop(request);
    }

    @Test
    void testAck() {
        com.wolfmq.domain.domain.consumer.ack.broker.AckRequest request =
            com.wolfmq.domain.domain.consumer.ack.broker.AckRequest.builder()
                .messageId("msg123")
                .build();

        consumer.ack(request);

        verify(ackService).ack(request);
    }

    @Test
    void testChangeInvisible() {
        com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest request =
            com.wolfmq.domain.domain.consumer.ack.broker.InvisibleRequest.builder()
                .messageId("msg123")
                .build();

        consumer.changeInvisible(request);

        verify(invisibleService).changeInvisible(request);
    }
}
