package com.wolfmq.broker.server.grpc;

import com.wolfmq.broker.server.grpc.activity.ClientActivity;
import com.wolfmq.broker.server.grpc.activity.ConsumerActivity;
import com.wolfmq.broker.server.grpc.activity.ProducerActivity;
import com.wolfmq.broker.server.grpc.activity.RouteActivity;
import com.wolfmq.broker.server.grpc.activity.TransactionActivity;
import com.wolfmq.rpc.common.grpc.RequestPipeline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GrpcMessageServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        ClientActivity clientActivity = mock(ClientActivity.class);
        ProducerActivity producerActivity = mock(ProducerActivity.class);
        RouteActivity routeActivity = mock(RouteActivity.class);
        ConsumerActivity consumerActivity = mock(ConsumerActivity.class);
        TransactionActivity transactionActivity = mock(TransactionActivity.class);

        GrpcMessageService service = new GrpcMessageService(
            clientActivity, routeActivity, producerActivity,
            consumerActivity, transactionActivity
        );
        assertNotNull(service);
    }
}
