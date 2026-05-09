package cn.coderule.wolfmq.rpc.registry.protocol.body;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TopicListTest {

    @Test
    void testDefaultConstructor() {
        TopicList topicList = new TopicList();
        
        assertNotNull(topicList);
        assertNotNull(topicList.getTopicList());
        assertTrue(topicList.getTopicList().isEmpty());
        assertNull(topicList.getBrokerAddr());
    }

    @Test
    void testSetAndGetTopicList() {
        TopicList topicList = new TopicList();
        Set<String> topics = Set.of("topic1", "topic2", "topic3");
        
        topicList.setTopicList(topics);
        
        assertEquals(topics, topicList.getTopicList());
        assertTrue(topicList.getTopicList().contains("topic1"));
        assertTrue(topicList.getTopicList().contains("topic2"));
    }

    @Test
    void testSetAndGetBrokerAddr() {
        TopicList topicList = new TopicList();
        String addr = "127.0.0.1:10911";
        
        topicList.setBrokerAddr(addr);
        
        assertEquals(addr, topicList.getBrokerAddr());
    }

    @Test
    void testInheritance() {
        TopicList topicList = new TopicList();
        
        assertTrue(topicList instanceof cn.coderule.wolfmq.rpc.common.rpc.protocol.codec.RpcSerializable);
    }

    @Test
    void testConcurrentModification() {
        TopicList topicList = new TopicList();
        topicList.getTopicList().add("topic1");
        topicList.getTopicList().add("topic2");
        
        assertEquals(2, topicList.getTopicList().size());
    }
}
