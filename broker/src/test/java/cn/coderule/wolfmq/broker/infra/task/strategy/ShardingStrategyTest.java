package cn.coderule.wolfmq.broker.infra.task.strategy;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShardingStrategyTest {

    @Test
    void testConstructor() {
        TaskContext taskContext = mock(TaskContext.class);
        ShardingStrategy strategy = new ShardingStrategy(taskContext);

        assertNotNull(strategy);
    }

    @Test
    void testLoad_doesNothing() {
        TaskContext taskContext = mock(TaskContext.class);
        ShardingStrategy strategy = new ShardingStrategy(taskContext);

        assertDoesNotThrow(() -> strategy.load());
    }
}