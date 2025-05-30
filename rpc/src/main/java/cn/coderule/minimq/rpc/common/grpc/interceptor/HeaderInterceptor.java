
package cn.coderule.minimq.rpc.common.grpc.interceptor;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcKeys;
import cn.coderule.minimq.rpc.common.grpc.core.constants.GrpcConstants;
import cn.coderule.minimq.rpc.common.core.constants.HAProxyConstants;
import com.google.common.net.HostAndPort;
import io.grpc.Attributes;
import io.grpc.Grpc;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderInterceptor implements ServerInterceptor {
    @Override
    public <R, W> ServerCall.Listener<R> interceptCall(ServerCall<R, W> call, Metadata headers, ServerCallHandler<R, W> next) {
        String remoteAddress = getProxyProtocolAddress(call.getAttributes());
        if (StringUtil.isBlank(remoteAddress)) {
            SocketAddress remoteSocketAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
            remoteAddress = parseSocketAddress(remoteSocketAddress);
        }
        headers.put(GrpcConstants.REMOTE_ADDRESS, remoteAddress);

        SocketAddress localSocketAddress = call.getAttributes().get(Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        String localAddress = parseSocketAddress(localSocketAddress);
        headers.put(GrpcConstants.LOCAL_ADDRESS, localAddress);

        for (Attributes.Key<?> key : call.getAttributes().keys()) {
            if (!StringUtil.startsWith(key.toString(), HAProxyConstants.PROXY_PROTOCOL_PREFIX)) {
                continue;
            }
            Metadata.Key<String> headerKey = Metadata.Key.of(key.toString(), Metadata.ASCII_STRING_MARSHALLER);
            String headerValue = String.valueOf(call.getAttributes().get(key));
            headers.put(headerKey, headerValue);
        }

        String channelId = call.getAttributes().get(GrpcKeys.CHANNEL_ID);
        if (StringUtil.notBlank(channelId)) {
            headers.put(GrpcConstants.CHANNEL_ID, channelId);
        }

        return next.startCall(call, headers);
    }

    private String parseSocketAddress(SocketAddress socketAddress) {
        if (!(socketAddress instanceof InetSocketAddress inetSocketAddress)) {
            return "";
        }

        return HostAndPort.fromParts(
            inetSocketAddress.getAddress().getHostAddress(),
            inetSocketAddress.getPort()
        ).toString();
    }

    private String getProxyProtocolAddress(Attributes attributes) {
        String proxyProtocolAddr = attributes.get(GrpcKeys.PROXY_PROTOCOL_ADDR);
        String proxyProtocolPort = attributes.get(GrpcKeys.PROXY_PROTOCOL_PORT);
        if (StringUtil.isBlank(proxyProtocolAddr) || StringUtil.isBlank(proxyProtocolPort)) {
            return null;
        }
        return proxyProtocolAddr + ":" + proxyProtocolPort;
    }
}
