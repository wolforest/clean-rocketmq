package cn.coderule.minimq.rpc.common.grpc.core.constants;

import cn.coderule.minimq.rpc.common.core.constants.HAProxyConstants;
import io.grpc.Attributes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GrpcKeys {

    public static final Attributes.Key<String> CHANNEL_ID =
        Attributes.Key.create(HAProxyConstants.CHANNEL_ID);

    public static final Attributes.Key<String> PROXY_PROTOCOL_ADDR =
            Attributes.Key.create(HAProxyConstants.PROXY_PROTOCOL_ADDR);

    public static final Attributes.Key<String> PROXY_PROTOCOL_PORT =
            Attributes.Key.create(HAProxyConstants.PROXY_PROTOCOL_PORT);

    public static final Attributes.Key<String> PROXY_PROTOCOL_SERVER_ADDR =
            Attributes.Key.create(HAProxyConstants.PROXY_PROTOCOL_SERVER_ADDR);

    public static final Attributes.Key<String> PROXY_PROTOCOL_SERVER_PORT =
            Attributes.Key.create(HAProxyConstants.PROXY_PROTOCOL_SERVER_PORT);

    private static final Map<String, Attributes.Key<String>> ATTRIBUTES_KEY_MAP = new ConcurrentHashMap<>();

    public static Attributes.Key<String> valueOf(String name) {
        return ATTRIBUTES_KEY_MAP.computeIfAbsent(name, key -> Attributes.Key.create(name));
    }
}
