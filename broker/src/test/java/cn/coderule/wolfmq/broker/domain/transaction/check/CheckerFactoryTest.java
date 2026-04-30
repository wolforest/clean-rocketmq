package cn.coderule.wolfmq.broker.domain.transaction.check;

import cn.coderule.wolfmq.broker.domain.transaction.check.context.TransactionContext;
import cn.coderule.wolfmq.domain.domain.cluster.task.QueueTask;
import cn.coderule.wolfmq.domain.domain.transaction.CommitBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CheckerFactoryTest {

    @Mock
    private TransactionContext transactionContext;

    @Mock
    private CommitBuffer commitBuffer;

    private CheckerFactory checkerFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(transactionContext.getCommitBuffer()).thenReturn(commitBuffer);
        checkerFactory = new CheckerFactory(transactionContext);
    }

    @Test
    void testConstructor() {
        assertNotNull(checkerFactory);
    }

    @Test
    void testStart() {
        assertDoesNotThrow(() -> checkerFactory.start());
    }

    @Test
    void testShutdown() {
        assertDoesNotThrow(() -> checkerFactory.shutdown());
    }
}
