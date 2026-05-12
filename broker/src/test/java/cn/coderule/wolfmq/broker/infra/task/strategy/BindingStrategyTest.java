package cn.coderule.wolfmq.broker.infra.task.strategy;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BindingStrategyTest {

    @Test
    void testConstructor() {
        TaskContext taskContext = mock(TaskContext.class);
        BindingStrategy strategy = new BindingStrategy(taskContext);

        assertNotNull(strategy);
    }

    @Test
    void testLoad_doesNothing() {
        TaskContext taskContext = mock(TaskContext.class);
        BindingStrategy strategy = new BindingStrategy(taskContext);

        assertDoesNotThrow(() -> strategy.load());
    }
}