package com.wolfmq.broker.domain.consumer.ack;

import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import com.wolfmq.rpc.store.facade.MQFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrokerAckServiceTest {

    @Mock
    private MQFacade mqStore;

    @Mock
    private ConsumerManager consumerManager;

    @Mock
    private ReceiptHandler receiptHandler;

    @Mock
    private AckValidator ackValidator;

    private BrokerAckService brokerAckService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        brokerAckService = new BrokerAckService(mqStore, consumerManager, receiptHandler, ackValidator);
    }

    @Test
    void testConstructor() {
        assertNotNull(brokerAckService);
    }
}
