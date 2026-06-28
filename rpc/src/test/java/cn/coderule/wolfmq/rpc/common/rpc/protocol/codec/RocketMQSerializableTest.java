package cn.coderule.wolfmq.rpc.common.rpc.protocol.codec;

import cn.coderule.wolfmq.rpc.common.rpc.core.invoke.RpcCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RocketMQSerializableTest {

    @Test
    void rocketMQProtocolEncode_ShouldEncodeCommand() {
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setBody(new byte[]{1, 2, 3});
        ByteBuf buf = Unpooled.buffer(256);

        int size = RocketMQSerializable.rocketMQProtocolEncode(cmd, buf);

        assertTrue(size > 0);
        buf.release();
    }

    @Test
    void rocketMQProtocolEncode_WithNullBody_ShouldEncode() {
        RpcCommand cmd = RpcCommand.createRequestCommand(200);
        ByteBuf buf = Unpooled.buffer(256);

        int size = RocketMQSerializable.rocketMQProtocolEncode(cmd, buf);

        assertTrue(size > 0);
        buf.release();
    }

    @Test
    void rocketMQProtocolEncode_WithRemark_ShouldEncode() {
        RpcCommand cmd = RpcCommand.createRequestCommand(300);
        cmd.setRemark("test remark");
        cmd.setBody(new byte[0]);
        ByteBuf buf = Unpooled.buffer(256);

        int size = RocketMQSerializable.rocketMQProtocolEncode(cmd, buf);

        assertTrue(size > 0);
        buf.release();
    }

    @Test
    void writeStr_ShouldWriteAndReadBack() {
        ByteBuf buf = Unpooled.buffer(256);
        String testStr = "hello world";
        RocketMQSerializable.writeStr(buf, false, testStr);

        int len = buf.readInt();
        assertEquals(testStr.length(), len);

        CharSequence cs = buf.readCharSequence(len, StandardCharsets.UTF_8);
        assertEquals(testStr, cs.toString());
        buf.release();
    }

    @Test
    void writeStr_ShortLength_ShouldUseShort() {
        ByteBuf buf = Unpooled.buffer(256);
        String testStr = "short";
        RocketMQSerializable.writeStr(buf, true, testStr);

        int len = buf.readShort();
        assertEquals(testStr.length(), len);

        CharSequence cs = buf.readCharSequence(len, StandardCharsets.UTF_8);
        assertEquals(testStr, cs.toString());
        buf.release();
    }

    @Test
    void rocketMQProtocolEncode_ByteArray_ShouldReturnBytes() {
        RpcCommand cmd = RpcCommand.createRequestCommand(100);
        cmd.setBody(new byte[]{1, 2, 3});

        byte[] bytes = RocketMQSerializable.rocketMQProtocolEncode(cmd);

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }
}