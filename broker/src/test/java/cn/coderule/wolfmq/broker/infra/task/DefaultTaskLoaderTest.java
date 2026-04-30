package cn.coderule.wolfmq.broker.infra.task;

import cn.coderule.wolfmq.domain.config.business.TaskConfig;
import cn.coderule.wolfmq.domain.config.business.TopicConfig;
import cn.coderule.wolfmq.domain.config.server.BrokerConfig;
import cn.coderule.wolfmq.domain.domain.cluster.task.TaskFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultTaskLoaderTest {

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
    private DefaultTaskLoader taskLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(brokerConfig.getTaskConfig()).thenReturn(taskConfig);
        when(brokerConfig.getTopicConfig()).thenReturn(topicConfig);
        when(brokerConfig.isEnableEmbedStore()).thenReturn(true);
        when(taskConfig.getTaskMode()).thenReturn("embed");
        when(topicConfig.getReviveQueueNum()).thenReturn(8);
        when(topicConfig.getTimerQueueNum()).thenReturn(4);
        when(topicConfig.getPrepareQueueNum()).thenReturn(2);
        
        taskContext = new TaskContext(brokerConfig);
        taskLoader = new DefaultTaskLoader(taskContext);
    }

    @Test
    void testConstructor() {
        assertNotNull(taskLoader);
    }

    @Test
    void testRegisterTimerFactory() {
        taskLoader.registerTimerFactory(timerFactory);
        assertEquals(timerFactory, taskContext.getTimerFactory());
    }

    @Test
    void testRegisterReviveFactory() {
        taskLoader.registerReviveFactory(reviveFactory);
        assertEquals(reviveFactory, taskContext.getReviveFactory());
    }

    @Test
    void testRegisterTransactionFactory() {
        taskLoader.registerTransactionFactory(transactionFactory);
        assertEquals(transactionFactory, taskContext.getTransactionFactory());
    }

    @Test
    void testLoadWithEmbedStrategy() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(true);
        
        // Should not throw
        assertDoesNotThrow(() -> taskLoader.load());
    }

    @Test
    void testLoadWithBindingStrategy() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);
        when(taskConfig.getTaskMode()).thenReturn("binding");
        
        // Should not throw
        assertDoesNotThrow(() -> taskLoader.load());
    }

    @Test
    void testLoadWithShardingStrategy() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);
        when(taskConfig.getTaskMode()).thenReturn("sharding");
        
        // Should not throw
        assertDoesNotThrow(() -> taskLoader.load());
    }

    @Test
    void testLoadWithNoStrategy() {
        when(brokerConfig.isEnableEmbedStore()).thenReturn(false);
        when(taskConfig.getTaskMode()).thenReturn("unknown");
        
        // Should not throw even with no matching strategy
        assertDoesNotThrow(() -> taskLoader.load());
    }
}
