package cn.coderule.wolfmq.rpc.store.protocol.header;

import cn.coderule.wolfmq.domain.core.enums.message.TagType;
import cn.coderule.wolfmq.domain.domain.meta.topic.Topic;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CreateTopicRequestHeaderTest {

    @Test
    void testDefaultConstructor() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        
        assertNotNull(header);
        assertNull(header.getTopic());
        assertNull(header.getDefaultTopic());
        assertNull(header.getReadQueueNums());
        assertNull(header.getWriteQueueNums());
        assertNull(header.getPerm());
        assertNull(header.getTopicFilterType());
        assertNull(header.getTopicSysFlag());
        assertEquals(Boolean.FALSE, header.getOrder());
        assertNull(header.getAttributes());
        assertEquals(Boolean.FALSE, header.getForce());
        assertNull(header.getLo());
    }

    @Test
    void testSettersAndGetters() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        
        header.setTopic("test-topic");
        header.setDefaultTopic("TBW102");
        header.setReadQueueNums(8);
        header.setWriteQueueNums(8);
        header.setPerm(6);
        header.setTopicFilterType("TAG");
        header.setTopicSysFlag(0);
        header.setOrder(true);
        header.setAttributes("key=value");
        header.setForce(true);
        header.setLo(true);
        
        assertEquals("test-topic", header.getTopic());
        assertEquals("TBW102", header.getDefaultTopic());
        assertEquals(8, header.getReadQueueNums());
        assertEquals(8, header.getWriteQueueNums());
        assertEquals(6, header.getPerm());
        assertEquals("TAG", header.getTopicFilterType());
        assertEquals(0, header.getTopicSysFlag());
        assertTrue(header.getOrder());
        assertEquals("key=value", header.getAttributes());
        assertTrue(header.getForce());
        assertTrue(header.getLo());
    }

    @Test
    void testToTopic() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopic("test-topic");
        header.setReadQueueNums(16);
        header.setWriteQueueNums(16);
        header.setPerm(7);
        header.setOrder(true);
        header.setTopicSysFlag(1);
        header.setAttributes("+cleanup.policy=delete");
        
        Topic topic = header.toTopic();
        
        assertNotNull(topic);
        assertEquals("test-topic", topic.getTopicName());
        assertEquals(16, topic.getReadQueueNums());
        assertEquals(16, topic.getWriteQueueNums());
        assertEquals(7, topic.getPerm());
        assertTrue(topic.isOrder());
        assertEquals(1, topic.getTopicSysFlag());
        assertNotNull(topic.getAttributes());
    }

    @Test
    void testToTopicWithNullSysFlag() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopic("test-topic");
        header.setReadQueueNums(8);
        header.setWriteQueueNums(8);
        header.setPerm(6);
        header.setOrder(false);
        header.setTopicSysFlag(null);
        
        Topic topic = header.toTopic();
        
        assertEquals(0, topic.getTopicSysFlag());
    }

    @Test
    void testCheckFieldsWithValidTagType() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopicFilterType("SINGLE_TAG");
        
        assertDoesNotThrow(() -> header.checkFields());
    }

    @Test
    void testCheckFieldsWithInvalidTagType() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopicFilterType("INVALID_TYPE");
        
        assertThrows(RemotingCommandException.class, () -> header.checkFields());
    }

    @Test
    void testGetTopicFilterTypeEnum() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopicFilterType("MULTI_TAG");
        
        TagType type = header.getTopicFilterTypeEnum();
        
        assertEquals(TagType.MULTI_TAG, type);
    }

    @Test
    void testToString() {
        CreateTopicRequestHeader header = new CreateTopicRequestHeader();
        header.setTopic("test-topic");
        header.setDefaultTopic("TBW102");
        
        String str = header.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("test-topic"));
        assertTrue(str.contains("TBW102"));
    }
}
