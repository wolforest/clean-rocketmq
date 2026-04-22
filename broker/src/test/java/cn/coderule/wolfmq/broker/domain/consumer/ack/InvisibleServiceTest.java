package cn.coderule.wolfmq.broker.domain.consumer.ack;

import cn.coderule.wolfmq.broker.domain.consumer.consumer.ConsumerManager;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.rpc.store.facade.MQFacade;
import cn.coderule.wolfmq.domain.domain.consumer.receipt.ReceiptHandler;
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
