package cn.coderule.wolfmq.broker.infra.task.strategy;

import cn.coderule.wolfmq.broker.infra.task.TaskContext;
import cn.coderule.wolfmq.domain.config.business.TaskConfig;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.QueueTask;
import cn.coderule.wolfmq.domain.domain.cluster.task.StoreTask;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedStrategyTest {

    @Mock
    private BrokerConfig brokerConfig;

    @Mock
    private TaskConfig taskConfig;

    @Mock
    private TopicConfig topicConfig;

    @Mock
    private TaskFactory timerFactory;

    @Mock
    private TaskFactory reviveFactory;

    @Mock
    private TaskFactory transactionFactory;

    private TaskContext taskContext;
    private EmbedStrategy embedStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        when(brokerConfig.getGroup()).thenReturn("testGroup");
        when(brokerConfig.getTaskConfig()).thenReturn(taskConfig);
        when(brokerConfig.getTopicConfig()).thenReturn(topicConfig);
        when(topicConfig.getReviveQueueNum()).thenReturn(8);
        when(topicConfig.getTimerQueueNum()).thenReturn(4);
        when(topicConfig.getPrepareQueueNum()).thenReturn(2);
        
        taskContext = new TaskContext(brokerConfig);
        taskContext.setTimerFactory(timerFactory);
        taskContext.setReviveFactory(reviveFactory);
        taskContext.setTransactionFactory(transactionFactory);
        
        embedStrategy = new EmbedStrategy(taskContext);
    }

    @Test
    void testLoadInitializesStoreTask() {
        embedStrategy.load();
        
        StoreTask task = taskContext.getTask();
        assertNotNull(task);
        assertEquals("testGroup", task.getStoreGroup());
    }

    @Test
    void testLoadInitializesTimerQueueSet() {
        embedStrategy.load();
        
        StoreTask task = taskContext.getTask();
        assertNotNull(task.getTimerQueueSet());
        assertEquals(4, task.getTimerQueueSet().size());
        assertTrue(task.getTimerQueueSet().contains(0));
        assertTrue(task.getTimerQueueSet().contains(3));
    }

    @Test
    void testLoadInitializesReviveQueueSet() {
        embedStrategy.load();
        
        StoreTask task = taskContext.getTask();
        assertNotNull(task.getReviveQueueSet());
        assertEquals(8, task.getReviveQueueSet().size());
        assertTrue(task.getReviveQueueSet().contains(0));
        assertTrue(task.getReviveQueueSet().contains(7));
    }

    @Test
    void testLoadInitializesTransactionQueueSet() {
        embedStrategy.load();
        
        StoreTask task = taskContext.getTask();
        assertNotNull(task.getTransactionQueueSet());
        assertEquals(2, task.getTransactionQueueSet().size());
        assertTrue(task.getTransactionQueueSet().contains(0));
        assertTrue(task.getTransactionQueueSet().contains(1));
    }

    @Test
    void testLoadCreatesTimerTasks() {
        embedStrategy.load();
        
        ArgumentCaptor<QueueTask> queueTaskCaptor = ArgumentCaptor.forClass(QueueTask.class);
        verify(timerFactory, times(4)).create(queueTaskCaptor.capture());
        
        java.util.List<QueueTask> capturedTasks = queueTaskCaptor.getAllValues();
        assertEquals(4, capturedTasks.size());
        assertEquals("testGroup", capturedTasks.get(0).getStoreGroup());
    }

    @Test
    void testLoadCreatesReviveTasks() {
        embedStrategy.load();
        
        ArgumentCaptor<QueueTask> queueTaskCaptor = ArgumentCaptor.forClass(QueueTask.class);
        verify(reviveFactory, times(8)).create(queueTaskCaptor.capture());
        
        java.util.List<QueueTask> capturedTasks = queueTaskCaptor.getAllValues();
        assertEquals(8, capturedTasks.size());
    }

    @Test
    void testLoadCreatesTransactionTasks() {
        embedStrategy.load();
        
        ArgumentCaptor<QueueTask> queueTaskCaptor = ArgumentCaptor.forClass(QueueTask.class);
        verify(transactionFactory, times(2)).create(queueTaskCaptor.capture());
        
        java.util.List<QueueTask> capturedTasks = queueTaskCaptor.getAllValues();
        assertEquals(2, capturedTasks.size());
    }

    @Test
    void testLoadSkipsTimerTaskWhenFactoryNull() {
        taskContext.setTimerFactory(null);
        
        embedStrategy.load();
        
        verify(timerFactory, never()).create(any());
    }

    @Test
    void testLoadSkipsReviveTaskWhenFactoryNull() {
        taskContext.setReviveFactory(null);
        
        embedStrategy.load();
        
        verify(reviveFactory, never()).create(any());
    }

    @Test
    void testLoadSkipsTransactionTaskWhenFactoryNull() {
        taskContext.setTransactionFactory(null);
        
        embedStrategy.load();
        
        verify(transactionFactory, never()).create(any());
    }
}
