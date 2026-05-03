package cn.coderule.wolfmq.broker.domain.meta;

import cn.coderule.wolfmq.domain.core.enums.message.MessageType;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.meta.topic.TopicRequest;
import cn.coderule.wolfmq.rpc.store.facade.TopicFacade;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BrokerTopicServiceTest {

    @Test
    void testExists() {
        TopicFacade store = mock(TopicFacade.class);
        when(store.exists("topic1")).thenReturn(true);
        BrokerTopicService service = new BrokerTopicService(store);

        assertTrue(service.exists("topic1"));
        assertFalse(service.exists("topic2"));
        verify(store, times(2)).exists(anyString());
    }

    @Test
    void testGetTopic() {
        TopicFacade store = mock(TopicFacade.class);
        Topic topic = mock(Topic.class);
        when(store.getTopic("topic1")).thenReturn(topic);
        BrokerTopicService service = new BrokerTopicService(store);

        assertEquals(topic, service.getTopic("topic1"));
    }

    @Test
    void testSaveTopic() {
        TopicFacade store = mock(TopicFacade.class);
        BrokerTopicService service = new BrokerTopicService(store);

        TopicRequest request = mock(TopicRequest.class);
        service.saveTopic(request);
        verify(store).saveTopic(request);
    }

    @Test
    void testDeleteTopic() {
        TopicFacade store = mock(TopicFacade.class);
        BrokerTopicService service = new BrokerTopicService(store);

        TopicRequest request = mock(TopicRequest.class);
        service.deleteTopic(request);
        verify(store).deleteTopic(request);
    }

    @Test
    void testGetTopicTypeNull() {
        TopicFacade store = mock(TopicFacade.class);
        when(store.getTopic("unknown")).thenReturn(null);
        BrokerTopicService service = new BrokerTopicService(store);

        assertEquals(MessageType.UNKNOWN, service.getTopicType("unknown"));
    }

    @Test
    void testGetTopicType() {
        TopicFacade store = mock(TopicFacade.class);
        Topic topic = mock(Topic.class);
        when(topic.getTopicType()).thenReturn(MessageType.NORMAL);
        when(store.getTopic("topic1")).thenReturn(topic);
        BrokerTopicService service = new BrokerTopicService(store);

        assertEquals(MessageType.NORMAL, service.getTopicType("topic1"));
    }
}