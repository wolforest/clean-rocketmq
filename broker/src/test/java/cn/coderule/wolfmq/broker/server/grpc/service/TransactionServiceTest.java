package cn.coderule.wolfmq.broker.server.grpc.service;

import cn.coderule.wolfmq.broker.api.TransactionController;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {

    @Test
    void constructor_ShouldCreateService() {
        TransactionController controller = mock(TransactionController.class);
        TransactionService service = new TransactionService(controller);
        assertNotNull(service);
    }
}
