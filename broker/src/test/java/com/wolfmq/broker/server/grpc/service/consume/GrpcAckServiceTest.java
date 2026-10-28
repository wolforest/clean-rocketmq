package com.wolfmq.broker.server.grpc.service.consume;

import com.wolfmq.broker.api.ConsumerController;
import com.wolfmq.domain.config.server.BrokerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class GrpcAckServiceTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private ConsumerController consumerController;

    private GrpcAckService service;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        service = new GrpcAckService(brokerConfig, consumerController);
    }

    @Test
    void testConstructorNotNull() {
        assertNotNull(service);
    }
}