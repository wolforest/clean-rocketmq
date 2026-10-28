package com.wolfmq.broker.domain.consumer.ack;

import com.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import com.wolfmq.domain.config.server.BrokerConfig;
import com.wolfmq.rpc.store.facade.MQFacade;
import com.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvisibleServiceTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private MQFacade mqStore;

    @Mock
    private ConsumerManager consumerManager;

    @Mock
    private ReceiptHandler receiptHandler;

    @Mock
    private AckValidator ackValidator;

    private InvisibleService invisibleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        invisibleService = new InvisibleService(
            brokerConfig, mqStore, consumerManager, receiptHandler, ackValidator
        );
    }

    @Test
    void testConstructor() {
        assertNotNull(invisibleService);
    }
}
