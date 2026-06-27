package cn.coderule.wolfmq.rpc.common.rpc.protocol.header;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpcRequestHeaderTest {

    static class TestHeader extends RpcRequestHeader {
        @Override
        public void checkFields() {}
    }

    @Test
    void gettersAndSetters() {
        TestHeader header = new TestHeader();
        header.setNamespace("ns1");
        header.setBrokerName("broker1");
        header.setNamespaced(true);
        header.setOneway(false);

        assertEquals("ns1", header.getNamespace());
        assertEquals("broker1", header.getBrokerName());
        assertTrue(header.getNamespaced());
        assertFalse(header.getOneway());
    }

    @Test
    void equalsAndHashCode() {
        TestHeader h1 = new TestHeader();
        h1.setNamespace("ns");
        h1.setBrokerName("bn");
        TestHeader h2 = new TestHeader();
        h2.setNamespace("ns");
        h2.setBrokerName("bn");

        assertEquals(h1, h2);
        assertEquals(h1.hashCode(), h2.hashCode());
    }

    @Test
    void equals_SameInstance() {
        TestHeader h = new TestHeader();
        assertEquals(h, h);
    }

    @Test
    void equals_NullAndDifferentClass() {
        TestHeader h = new TestHeader();
        assertNotEquals(h, null);
        assertNotEquals(h, "string");
    }

    @Test
    void toString_ShouldContainFields() {
        TestHeader h = new TestHeader();
        h.setNamespace("ns1");
        String str = h.toString();
        assertTrue(str.contains("ns1"));
    }
}