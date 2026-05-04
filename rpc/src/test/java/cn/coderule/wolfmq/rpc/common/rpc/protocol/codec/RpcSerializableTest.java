package cn.coderule.wolfmq.rpc.common.rpc.protocol.codec;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class RpcSerializableTest {

    static class TestPojo {
        private String name;
        private int value;

        public TestPojo() {}

        public TestPojo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getValue() { return value; }
        public void setValue(int value) { this.value = value; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestPojo testPojo = (TestPojo) o;
            return value == testPojo.value && Objects.equals(name, testPojo.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, value);
        }
    }

    @Test
    void encodeNonNullReturnsByteArray() {
        TestPojo pojo = new TestPojo("test", 42);
        byte[] encoded = RpcSerializable.encode(pojo);
        assertNotNull(encoded);
        assertTrue(encoded.length > 0);
    }

    @Test
    void encodeNullReturnsNull() {
        assertNull(RpcSerializable.encode((Object) null));
    }

    @Test
    void decodeEncodeRoundtrip() {
        TestPojo original = new TestPojo("hello", 123);
        byte[] encoded = RpcSerializable.encode(original);
        TestPojo decoded = RpcSerializable.decode(encoded, TestPojo.class);
        assertEquals(original, decoded);
    }

    @Test
    void decodeNullReturnsNull() {
        assertNull(RpcSerializable.decode(null, TestPojo.class));
    }

    @Test
    void decodeListNullReturnsNull() {
        assertNull(RpcSerializable.decodeList(null, TestPojo.class));
    }

    @Test
    void decodeListRoundtrip() {
        List<TestPojo> original = List.of(
            new TestPojo("a", 1),
            new TestPojo("b", 2)
        );
        byte[] encoded = RpcSerializable.encode(original);
        List<TestPojo> decoded = RpcSerializable.decodeList(encoded, TestPojo.class);
        assertEquals(2, decoded.size());
        assertEquals("a", decoded.get(0).getName());
        assertEquals(1, decoded.get(0).getValue());
        assertEquals("b", decoded.get(1).getName());
        assertEquals(2, decoded.get(1).getValue());
    }

    @Test
    void toJsonReturnsValidJsonString() {
        TestPojo pojo = new TestPojo("test", 42);
        String json = RpcSerializable.toJson(pojo);
        assertNotNull(json);
        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void fromJsonParsesCorrectly() {
        String json = "{\"name\":\"hello\",\"value\":99}";
        TestPojo result = RpcSerializable.fromJson(json, TestPojo.class);
        assertEquals("hello", result.getName());
        assertEquals(99, result.getValue());
    }
}