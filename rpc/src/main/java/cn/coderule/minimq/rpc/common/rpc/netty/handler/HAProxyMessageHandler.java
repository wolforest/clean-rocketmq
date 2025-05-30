package cn.coderule.minimq.rpc.common.rpc.netty.handler;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.rpc.common.core.constants.HAProxyConstants;
import cn.coderule.minimq.rpc.common.rpc.core.constant.AttributeKeys;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;

public class HAProxyMessageHandler extends ChannelInboundHandlerAdapter {

    public HAProxyMessageHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HAProxyMessage) {
            handleWithMessage((HAProxyMessage) msg, ctx.channel());
        } else {
            super.channelRead(ctx, msg);
        }
        ctx.pipeline().remove(this);
    }

    /**
     * The definition of key refers to the implementation of nginx
     * <a href="https://nginx.org/en/docs/http/ngx_http_core_module.html#var_proxy_protocol_addr">ngx_http_core_module</a>
     * @param msg msg
     * @param channel channel
     */
    private void handleWithMessage(HAProxyMessage msg, Channel channel) {
        try {
            if (StringUtil.notBlank(msg.sourceAddress())) {
                channel.attr(AttributeKeys.PROXY_PROTOCOL_ADDR).set(msg.sourceAddress());
            }
            if (msg.sourcePort() > 0) {
                channel.attr(AttributeKeys.PROXY_PROTOCOL_PORT).set(String.valueOf(msg.sourcePort()));
            }
            if (StringUtil.notBlank(msg.destinationAddress())) {
                channel.attr(AttributeKeys.PROXY_PROTOCOL_SERVER_ADDR).set(msg.destinationAddress());
            }
            if (msg.destinationPort() > 0) {
                channel.attr(AttributeKeys.PROXY_PROTOCOL_SERVER_PORT).set(String.valueOf(msg.destinationPort()));
            }
            if (CollectionUtil.notEmpty(msg.tlvs())) {
                msg.tlvs().forEach(tlv -> {
                    byte[] valueBytes = ByteBufUtil.getBytes(tlv.content());
                    if (!StringUtil.isAscii(valueBytes)) {
                        return;
                    }
                    AttributeKey<String> key = AttributeKeys.valueOf(
                        HAProxyConstants.PROXY_PROTOCOL_TLV_PREFIX + String.format("%02x", tlv.typeByteValue()));
                    channel.attr(key).set(new String(valueBytes, CharsetUtil.UTF_8));
                });
            }
        } finally {
            msg.release();
        }
    }

}


