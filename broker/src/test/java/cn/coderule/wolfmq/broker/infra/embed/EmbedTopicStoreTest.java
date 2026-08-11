package cn.coderule.wolfmq.broker.infra.embed;

import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.store.api.meta.TopicStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmbedTopicStoreTest {

    private EmbedTopicStore store;
    private TopicStore topicStore;
    private EmbedLoadBalance loadBalance;

    @BeforeEach
    void setUp() {
        topicStore = mock(TopicStore.class);
        loadBalance = mock(EmbedLoadBalance.class);
        store = new EmbedTopicStore(topicStore, loadBalance);
    }

    @Test
    void constructor_ShouldCreateStore() {
        assertNotNull(store);
    }

    @Test
    void exists_ShouldDelegate() {
        when(topicStore.exists("test-topic")).thenReturn(true);
        assertTrue(store.exists("test-topic"));
    }

    @Test
    void getTopic_ShouldDelegate() {
        Topic topic = new Topic();
        topic.setTopicName("test-topic");
        when(topicStore.getTopic("test-topic")).thenReturn(topic);
        Topic result = store.getTopic("test-topic");
        assertNotNull(result);
        assertEquals("test-topic", result.getTopicName());
    }
}
