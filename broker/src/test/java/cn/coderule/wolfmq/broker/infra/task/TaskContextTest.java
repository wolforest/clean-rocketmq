package cn.coderule.wolfmq.broker.infra.task;

import cn.coderule.wolfmq.domain.config.business.TaskConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.StoreTask;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskContextTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private TaskConfig taskConfig;

    @Mock
    private TaskFactory timerFactory;

    @Mock
    private TaskFactory reviveFactory;

    @Mock
    private TaskFactory transactionFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getTaskConfig()).thenReturn(taskConfig);
    }

    @Test
    void testConstructor() {
        TaskContext context = new TaskContext(brokerConfig);

        assertNotNull(context);
        assertEquals(brokerConfig, context.getBrokerConfig());
        assertEquals(taskConfig, context.getTaskConfig());
    }

    @Test
    void testTimerFactorySetterGetter() {
        TaskContext context = new TaskContext(brokerConfig);
        context.setTimerFactory(timerFactory);

        assertEquals(timerFactory, context.getTimerFactory());
    }

    @Test
    void testReviveFactorySetterGetter() {
        TaskContext context = new TaskContext(brokerConfig);
        context.setReviveFactory(reviveFactory);

        assertEquals(reviveFactory, context.getReviveFactory());
    }

    @Test
    void testTransactionFactorySetterGetter() {
        TaskContext context = new TaskContext(brokerConfig);
        context.setTransactionFactory(transactionFactory);

        assertEquals(transactionFactory, context.getTransactionFactory());
    }

    @Test
    void testTaskSetterGetter() {
        TaskContext context = new TaskContext(brokerConfig);
        StoreTask task = new StoreTask();
        context.setTask(task);

        assertEquals(task, context.getTask());
    }

    @Test
    void testInitialFactoriesAreNull() {
        TaskContext context = new TaskContext(brokerConfig);

        assertNull(context.getTimerFactory());
        assertNull(context.getReviveFactory());
        assertNull(context.getTransactionFactory());
        assertNull(context.getTask());
    }
}
