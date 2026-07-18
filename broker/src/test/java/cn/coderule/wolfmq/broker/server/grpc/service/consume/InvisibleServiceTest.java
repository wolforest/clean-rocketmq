package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import cn.coderule.wolfmq.broker.api.ConsumerController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InvisibleServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        ConsumerController consumerController = mock(ConsumerController.class);
        InvisibleService service = new InvisibleService(consumerController);
        assertNotNull(service);
    }
}
