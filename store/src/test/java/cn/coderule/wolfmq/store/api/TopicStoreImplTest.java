package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TopicStoreImplTest {

    private TopicService topicService;
    private TopicStoreImpl store;

    @BeforeEach
    void setUp() {
        topicService = mock(TopicService.class);
        store = new TopicStoreImpl(topicService);
    }

    @Test
    void exists_ShouldDelegateToService() {
        when(topicService.exists("t1")).thenReturn(true);

        assertTrue(store.exists("t1"));
        verify(topicService).exists("t1");
    }

    @Test
    void getTopic_ShouldDelegateToService() {
        Topic topic = new Topic();
        topic.setTopicName("t1");
        when(topicService.getTopic("t1")).thenReturn(topic);

        Topic result = store.getTopic("t1");

        assertEquals("t1", result.getTopicName());
        verify(topicService).getTopic("t1");
    }

    @Test
    void saveTopic_ShouldDelegateToService() {
        Topic topic = new Topic();
        topic.setTopicName("t1");

        store.saveTopic(topic);

        verify(topicService).saveTopic(topic);
    }

    @Test
    void deleteTopic_ShouldDelegateToService() {
        store.deleteTopic("t1");

        verify(topicService).deleteTopic("t1");
    }

    @Test
    void saveTopic_WithRequest_ShouldDelegateToService() {
        Topic topic = new Topic();
        topic.setTopicName("t1");
        TopicRequest request = TopicRequest.builder().topic(topic).build();

        store.saveTopic(request);

        verify(topicService).saveTopic(topic);
    }

    @Test
    void deleteTopic_WithRequest_ShouldDelegateToService() {
        TopicRequest request = TopicRequest.builder().topicName("t1").build();

        store.deleteTopic(request);

        verify(topicService).deleteTopic("t1");
    }

    @Test
    void getAllTopicJson_ShouldReturnEmpty() {
        assertEquals("", store.getAllTopicJson());
    }
}