package cn.coderule.wolfmq.broker.api;

import cn.coderule.wolfmq.broker.domain.transaction.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionControllerTest {

    private TransactionController controller;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        transaction = mock(Transaction.class);
        controller = new TransactionController(transaction);
    }

    @Test
    void constructor_ShouldCreateController() {
        assertNotNull(controller);
    }
}
