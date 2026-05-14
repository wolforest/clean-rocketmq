package cn.coderule.wolfmq.domain.domain.producer;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.domain.domain.MessageQueue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProduceContextTest {

    @Test
    void testBuilder() {
        RequestContext ctx = RequestContext.builder().build();
        MessageBO msg = mock(MessageBO.class);
        MessageQueue mq = mock(MessageQueue.class);
        Topic topic = mock(Topic.class);

        ProduceContext context = ProduceContext.builder()
            .requestContext(ctx)
            .messageBO(msg)
            .messageQueue(mq)
            .topic(topic)
            .namespace("ns")
            .producerGroup("pg")
            .topicName("test")
            .build();

        assertNotNull(context);
        assertEquals(ctx, context.getRequestContext());
        assertEquals(msg, context.getMessageBO());
        assertEquals(mq, context.getMessageQueue());
        assertEquals(topic, context.getTopic());
        assertEquals("ns", context.getNamespace());
        assertEquals("pg", context.getProducerGroup());
        assertEquals("test", context.getTopicName());
    }

    @Test
    void testNoArgsConstructor() {
        ProduceContext context = new ProduceContext();
        assertNotNull(context);
    }

    @Test
    void testSetters() {
        RequestContext ctx = RequestContext.builder().build();
        MessageBO msg = mock(MessageBO.class);
        MessageQueue mq = mock(MessageQueue.class);
        Topic topic = mock(Topic.class);

        ProduceContext context = new ProduceContext();
        context.setRequestContext(ctx);
        context.setMessageBO(msg);
        context.setMessageQueue(mq);
        context.setTopic(topic);
        context.setNamespace("ns");
        context.setProducerGroup("pg");
        context.setTopicName("test");

        assertEquals(ctx, context.getRequestContext());
        assertEquals(msg, context.getMessageBO());
        assertEquals(mq, context.getMessageQueue());
        assertEquals(topic, context.getTopic());
        assertEquals("ns", context.getNamespace());
        assertEquals("pg", context.getProducerGroup());
        assertEquals("test", context.getTopicName());
    }
}