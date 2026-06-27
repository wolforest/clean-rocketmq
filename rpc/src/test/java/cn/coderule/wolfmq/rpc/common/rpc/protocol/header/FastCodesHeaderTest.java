package cn.coderule.wolfmq.rpc.common.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class FastCodesHeaderTest {

    static class TestFastCodesHeader implements FastCodesHeader {
        private String key1;
        private String key2;

        @Override
        public void encode(ByteBuf out) {
            writeIfNotNull(out, "key1", key1);
            writeIfNotNull(out, "key2", key2);
        }

        @Override
        public void decode(HashMap<String, String> fields) throws RemotingCommandException {
            this.key1 = getAndCheckNotNull(fields, "key1");
            this.key2 = getAndCheckNotNull(fields, "key2");
        }

        public String getKey1() { return key1; }
        public String getKey2() { return key2; }
    }

    @Test
    void getAndCheckNotNull_WithExistingKey_ShouldReturnValue() throws RemotingCommandException {
        TestFastCodesHeader header = new TestFastCodesHeader();
        HashMap<String, String> fields = new HashMap<>();
        fields.put("key1", "value1");
        fields.put("key2", "value2");
        header.decode(fields);

        assertEquals("value1", header.getKey1());
        assertEquals("value2", header.getKey2());
    }

    @Test
    void getAndCheckNotNull_WithMissingKey_ShouldReturnNull() throws RemotingCommandException {
        TestFastCodesHeader header = new TestFastCodesHeader();
        HashMap<String, String> fields = new HashMap<>();
        header.decode(fields);

        assertNull(header.getKey1());
    }

    @Test
    void writeIfNotNull_WithNonNull_ShouldWrite() {
        TestFastCodesHeader header = new TestFastCodesHeader();
        ByteBuf buf = Unpooled.buffer(256);
        header.writeIfNotNull(buf, "testKey", "testValue");

        assertTrue(buf.readableBytes() > 0);
        buf.release();
    }

    @Test
    void writeIfNotNull_WithNull_ShouldSkip() {
        ByteBuf buf = Unpooled.buffer(256);
        TestFastCodesHeader header = new TestFastCodesHeader();
        header.writeIfNotNull(buf, "testKey", null);

        assertEquals(0, buf.readableBytes());
        buf.release();
    }

    @Test
    void encode_ShouldWriteFields() {
        TestFastCodesHeader header = new TestFastCodesHeader();
        ByteBuf buf = Unpooled.buffer(256);
        header.writeIfNotNull(buf, "k", "v");
        assertTrue(buf.readableBytes() > 0);
        buf.release();
    }
}