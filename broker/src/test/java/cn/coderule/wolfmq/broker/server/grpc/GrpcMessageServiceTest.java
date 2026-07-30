package cn.coderule.wolfmq.broker.server.grpc;

import cn.coderule.wolfmq.broker.server.grpc.activity.ClientActivity;
import cn.coderule.wolfmq.broker.server.grpc.activity.ConsumerActivity;
import cn.coderule.wolfmq.broker.server.grpc.activity.ProducerActivity;
import cn.coderule.wolfmq.broker.server.grpc.activity.RouteActivity;
import cn.coderule.wolfmq.broker.server.grpc.activity.TransactionActivity;
import cn.coderule.wolfmq.rpc.common.grpc.RequestPipeline;
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
        RequestPipeline pipeline = (ctx, headers, request) -> {};

        GrpcMessageService service = new GrpcMessageService(
            clientActivity, routeActivity, producerActivity,
            consumerActivity, transactionActivity, pipeline
        );
        assertNotNull(service);
    }
}
