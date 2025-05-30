package cn.coderule.minimq.rpc.common.rpc.netty.codec;

import cn.coderule.minimq.rpc.common.rpc.core.invoke.RpcCommand;
import cn.coderule.minimq.rpc.common.rpc.netty.service.NettyHelper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class NettyEncoder extends MessageToByteEncoder<RpcCommand> {

    @Override
    public void encode(ChannelHandlerContext ctx, RpcCommand remotingCommand, ByteBuf out)
        throws Exception {
        try {
            remotingCommand.fastEncodeHeader(out);
            byte[] body = remotingCommand.getBody();
            if (body != null) {
                out.writeBytes(body);
            }
        } catch (Exception e) {
            log.error("encode exception, {}", NettyHelper.getRemoteAddr(ctx.channel()), e);
            if (remotingCommand != null) {
                log.error(remotingCommand.toString());
            }
            NettyHelper.close(ctx.channel());
        }
    }
}
