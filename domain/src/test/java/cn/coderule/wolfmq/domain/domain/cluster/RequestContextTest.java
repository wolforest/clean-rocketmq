package cn.coderule.wolfmq.domain.domain.cluster;

import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {

    @Test
    void testDefaultConstructor() {
        RequestContext context = new RequestContext();
        assertNotNull(context);
        assertNotNull(context.getMap());
    }

    @Test
    void testCreateWithStoreGroup() {
        RequestContext context = RequestContext.create("testGroup");
        assertNotNull(context);
        assertEquals("testGroup", context.getStoreGroup());
    }

    @Test
    void testCreateEmpty() {
        RequestContext context = RequestContext.create();
        assertNotNull(context);
    }

    @Test
    void testCreateForInnerWithAction() {
        RequestContext context = RequestContext.createForInner("TestAction");
        assertNotNull(context);
        assertEquals(RequestContext.INNER_ACTION_PREFIX + "TestAction", context.getAction());
    }

    @Test
    void testCreateForInnerWithClass() {
        RequestContext context = RequestContext.createForInner(RequestContextTest.class);
        assertNotNull(context);
        assertEquals(RequestContext.INNER_ACTION_PREFIX + "RequestContextTest", context.getAction());
    }

    @Test
    void testSetAndGet() {
        RequestContext context = RequestContext.create();
        context.set("testKey", "testValue");
        
        assertEquals("testValue", context.get("testKey"));
    }

    @Test
    void testServerPort() {
        RequestContext context = RequestContext.create();
        context.setServerPort(10911);
        
        assertEquals(10911, context.getServerPort());
    }

    @Test
    void testLocalAddress() {
        RequestContext context = RequestContext.create();
        context.setLocalAddress("127.0.0.1:10911");
        
        assertEquals("127.0.0.1:10911", context.getLocalAddress());
    }

    @Test
    void testRemoteAddress() {
        RequestContext context = RequestContext.create();
        context.setRemoteAddress("192.168.1.1:12345");
        
        assertEquals("192.168.1.1:12345", context.getRemoteAddress());
    }

    @Test
    void testClientID() {
        RequestContext context = RequestContext.create();
        context.setClientID("client-001");
        
        assertEquals("client-001", context.getClientID());
    }

    @Test
    void testChannel() {
        RequestContext context = RequestContext.create();
        Channel channel = new EmbeddedChannel();
        context.setChannel(channel);
        
        assertEquals(channel, context.getChannel());
    }

    @Test
    void testLanguage() {
        RequestContext context = RequestContext.create();
        context.setLanguage("JAVA");
        
        assertEquals("JAVA", context.getLanguage());
    }

    @Test
    void testRequestTimeAutoSet() {
        RequestContext context = RequestContext.create();
        
        // getRequestTime 会自动设置当前时间
        Long time = context.getRequestTime();
        assertNotNull(time);
        assertTrue(time > 0);
        
        // 再次获取应该返回相同值
        assertEquals(time, context.getRequestTime());
    }

    @Test
    void testRequestTimeManualSet() {
        RequestContext context = RequestContext.create();
        context.setRequestTime(12345678L);
        
        assertEquals(12345678L, context.getRequestTime());
    }

    @Test
    void testClientVersion() {
        RequestContext context = RequestContext.create();
        context.setClientVersion("4.9.0");
        
        assertEquals("4.9.0", context.getClientVersion());
    }

    @Test
    void testRemainingMs() {
        RequestContext context = RequestContext.create();
        context.setRemainingMs(5000L);
        
        assertEquals(5000L, context.getRemainingMs());
    }

    @Test
    void testAction() {
        RequestContext context = RequestContext.create();
        context.setAction("SEND_MESSAGE");
        
        assertEquals("SEND_MESSAGE", context.getAction());
    }

    @Test
    void testProtocolType() {
        RequestContext context = RequestContext.create();
        context.setProtocolType("GRPC");
        
        assertEquals("GRPC", context.getProtocolType());
    }

    @Test
    void testNamespace() {
        RequestContext context = RequestContext.create();
        context.setNamespace("test-namespace");
        
        assertEquals("test-namespace", context.getNamespace());
    }

    @Test
    void testBuilder() {
        RequestContext context = RequestContext.builder()
            .storeGroup("broker-a")
            .consumeGroup("consumer-group-1")
            .build();
        
        assertNotNull(context);
        assertEquals("broker-a", context.getStoreGroup());
        assertEquals("consumer-group-1", context.getConsumeGroup());
    }

    @Test
    void testStoreGroupSetter() {
        RequestContext context = RequestContext.create();
        context.setStoreGroup("broker-b");
        
        assertEquals("broker-b", context.getStoreGroup());
    }

    @Test
    void testConsumeGroupSetter() {
        RequestContext context = RequestContext.create();
        context.setConsumeGroup("consumer-group-2");
        
        assertEquals("consumer-group-2", context.getConsumeGroup());
    }
}
