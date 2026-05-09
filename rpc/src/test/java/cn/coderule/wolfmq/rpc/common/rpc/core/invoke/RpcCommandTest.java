package cn.coderule.wolfmq.rpc.common.rpc.core.invoke;

import cn.coderule.wolfmq.domain.core.enums.code.LanguageCode;
import cn.coderule.wolfmq.rpc.common.rpc.core.enums.RemotingCommandType;
import cn.coderule.wolfmq.rpc.common.rpc.core.enums.SerializeType;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.ResponseCode;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.SystemResponseCode;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import cn.coderule.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;

public class RpcCommandTest {

    @Test
    void testDefaultConstructor() {
        RpcCommand cmd = new RpcCommand();
        
        assertNotNull(cmd);
        assertEquals(LanguageCode.JAVA, cmd.getLanguage());
        assertEquals(0, cmd.getVersion());
        assertTrue(cmd.getOpaque() >= 0);
    }

    @Test
    void testOpaqueIncrement() {
        RpcCommand cmd1 = new RpcCommand();
        RpcCommand cmd2 = new RpcCommand();
        
        assertNotEquals(cmd1.getOpaque(), cmd2.getOpaque());
        assertTrue(cmd2.getOpaque() > cmd1.getOpaque());
    }

    @Test
    void testCreateRequestCommand() {
        int code = 100;
        RpcCommand cmd = RpcCommand.createRequestCommand(code);
        
        assertNotNull(cmd);
        assertEquals(code, cmd.getCode());
        assertEquals(RemotingCommandType.REQUEST_COMMAND, cmd.getType());
        assertFalse(cmd.isResponseType());
    }

    @Test
    void testCreateRequestCommandWithHeader() {
        int code = 100;
        TestCommandHeader header = new TestCommandHeader();
        header.setTestField("test");
        
        RpcCommand cmd = RpcCommand.createRequestCommand(code, header);
        
        assertNotNull(cmd);
        assertEquals(code, cmd.getCode());
        assertNotNull(cmd.readCustomHeader());
    }

    @Test
    void testCreateResponseCommand() {
        RpcCommand cmd = RpcCommand.createResponseCommand(TestCommandHeader.class);
        
        assertNotNull(cmd);
        assertTrue(cmd.isResponseType());
        assertEquals(SystemResponseCode.SYSTEM_ERROR, cmd.getCode());
        assertNotNull(cmd.readCustomHeader());
    }

    @Test
    void testCreateResponseCommandWithCodeAndRemark() {
        int code = ResponseCode.SUCCESS;
        String remark = "Success";
        
        RpcCommand cmd = RpcCommand.createResponseCommand(code, remark);
        
        assertNotNull(cmd);
        assertTrue(cmd.isResponseType());
        assertEquals(code, cmd.getCode());
        assertEquals(remark, cmd.getRemark());
    }

    @Test
    void testCreateResponseCommandWithHeader() {
        int code = ResponseCode.SUCCESS;
        String remark = "Success";
        
        RpcCommand cmd = RpcCommand.createResponseCommand(code, remark, TestCommandHeader.class);
        
        assertNotNull(cmd);
        assertTrue(cmd.isResponseType());
        assertEquals(code, cmd.getCode());
        assertEquals(remark, cmd.getRemark());
        assertNotNull(cmd.readCustomHeader());
    }

    @Test
    void testBuildErrorResponse() {
        int code = ResponseCode.SYSTEM_ERROR;
        String remark = "Error occurred";
        
        RpcCommand cmd = RpcCommand.buildErrorResponse(code, remark);
        
        assertNotNull(cmd);
        assertEquals(code, cmd.getCode());
        assertEquals(remark, cmd.getRemark());
    }

    @Test
    void testSuccessAndFailure() {
        RpcCommand cmd = new RpcCommand();
        
        RpcCommand successCmd = cmd.success();
        assertNotNull(successCmd);
        assertEquals(ResponseCode.SUCCESS, successCmd.getCode());
        assertTrue(successCmd.isSuccess());
        
        RpcCommand failureCmd = cmd.failure(ResponseCode.SYSTEM_ERROR, "Failed");
        assertNotNull(failureCmd);
        assertEquals(ResponseCode.SYSTEM_ERROR, failureCmd.getCode());
        assertFalse(failureCmd.isSuccess());
    }

    @Test
    void testMarkResponseType() {
        RpcCommand cmd = new RpcCommand();
        assertFalse(cmd.isResponseType());
        
        cmd.markResponseType();
        assertTrue(cmd.isResponseType());
        assertEquals(RemotingCommandType.RESPONSE_COMMAND, cmd.getType());
    }

    @Test
    void testMarkOnewayRPC() {
        RpcCommand cmd = new RpcCommand();
        assertFalse(cmd.isOnewayRPC());
        
        cmd.markOnewayRPC();
        assertTrue(cmd.isOnewayRPC());
    }

    @Test
    void testAddExtField() {
        RpcCommand cmd = new RpcCommand();
        
        cmd.addExtField("key1", "value1");
        assertEquals("value1", cmd.getExtFields().get("key1"));
        
        cmd.addExtField("key2", "value2");
        assertEquals("value2", cmd.getExtFields().get("key2"));
    }

    @Test
    void testAddExtFieldIfNotExist() {
        RpcCommand cmd = new RpcCommand();
        cmd.addExtField("key", "original");
        
        cmd.addExtFieldIfNotExist("key", "new");
        assertEquals("original", cmd.getExtFields().get("key"));
        
        cmd.addExtFieldIfNotExist("newKey", "newValue");
        assertEquals("newValue", cmd.getExtFields().get("newKey"));
    }

    @Test
    void testEncodeAndDecodeHeader() throws RemotingCommandException {
        RpcCommand original = RpcCommand.createRequestCommand(100);
        original.setRemark("test remark");
        original.addExtField("extKey", "extValue");
        
        ByteBuffer encoded = original.encodeHeader();
        assertNotNull(encoded);
        assertTrue(encoded.remaining() > 0);
    }

    @Test
    void testDecodeHeader() throws RemotingCommandException {
        TestCommandHeader header = new TestCommandHeader();
        header.setTestField("testValue");
        header.setIntField(42);
        header.setLongField(123456L);
        header.setBooleanField(true);
        
        RpcCommand cmd = RpcCommand.createRequestCommand(100, header);
        cmd.makeCustomHeaderToNet();
        
        TestCommandHeader decoded = cmd.decodeHeader(TestCommandHeader.class);
        
        assertNotNull(decoded);
        assertEquals("testValue", decoded.getTestField());
        assertEquals(42, decoded.getIntField());
        assertEquals(123456L, decoded.getLongField());
        assertTrue(decoded.isBooleanField());
    }

    @Test
    void testMarkProtocolType() {
        int source = 100;
        SerializeType type = SerializeType.JSON;
        
        int result = RpcCommand.markProtocolType(source, type);
        
        assertEquals(SerializeType.JSON, RpcCommand.getProtocolType(result));
        assertEquals(source, RpcCommand.getHeaderLength(result));
    }

    @Test
    void testCreateNewRequestId() {
        int id1 = RpcCommand.createNewRequestId();
        int id2 = RpcCommand.createNewRequestId();
        
        assertTrue(id2 > id1);
    }

    @Test
    void testSetCodeAndRemark() {
        RpcCommand cmd = new RpcCommand();
        
        RpcCommand result = cmd.setCodeAndRemark(200, "OK");
        
        assertSame(cmd, result);
        assertEquals(200, cmd.getCode());
        assertEquals("OK", cmd.getRemark());
    }

    @Test
    void testWriteAndReadHeader() {
        RpcCommand cmd = new RpcCommand();
        TestCommandHeader header = new TestCommandHeader();
        header.setTestField("test");
        
        cmd.writeHeader(header);
        
        CommandHeader read = cmd.readCustomHeader();
        assertNotNull(read);
        assertTrue(read instanceof TestCommandHeader);
        assertEquals("test", ((TestCommandHeader) read).getTestField());
    }

    @Test
    void testEncodeHeader() {
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setRemark("test");
        
        ByteBuffer headerBuffer = cmd.encodeHeader();
        assertNotNull(headerBuffer);
        assertTrue(headerBuffer.remaining() > 0);
    }

    @Test
    void testFastEncodeHeader() {
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setBody("test".getBytes());
        
        ByteBuf out = Unpooled.buffer();
        cmd.fastEncodeHeader(out);
        
        assertTrue(out.readableBytes() > 0);
    }

    @Test
    void testToString() {
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setRemark("test");
        
        String str = cmd.toString();
        
        assertNotNull(str);
        assertTrue(str.contains("code=100"));
        assertTrue(str.contains("remark=test"));
    }

    @Test
    void testGetSerializeTypeConfigInThisServer() {
        SerializeType type = RpcCommand.getSerializeTypeConfigInThisServer();
        assertNotNull(type);
    }

    public static class TestCommandHeader implements CommandHeader {
        @CFNotNull
        private String testField;
        private int intField;
        private long longField;
        private boolean booleanField;

        public String getTestField() { return testField; }
        public void setTestField(String testField) { this.testField = testField; }
        public int getIntField() { return intField; }
        public void setIntField(int intField) { this.intField = intField; }
        public long getLongField() { return longField; }
        public void setLongField(long longField) { this.longField = longField; }
        public boolean isBooleanField() { return booleanField; }
        public void setBooleanField(boolean booleanField) { this.booleanField = booleanField; }

        @Override
        public void checkFields() {
        }
    }
}
