package cn.coderule.minimq.rpc.common.rpc.core.constant;

import cn.coderule.minimq.rpc.common.core.constants.HAProxyConstants;
import cn.coderule.minimq.domain.domain.enums.code.LanguageCode;
import io.netty.util.AttributeKey;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AttributeKeys {

    public static final AttributeKey<String> REMOTE_ADDR_KEY = AttributeKey.valueOf("RemoteAddr");

    public static final AttributeKey<String> CLIENT_ID_KEY = AttributeKey.valueOf("ClientId");

    public static final AttributeKey<Integer> VERSION_KEY = AttributeKey.valueOf("Version");

    public static final AttributeKey<LanguageCode> LANGUAGE_CODE_KEY = AttributeKey.valueOf("LanguageCode");

    public static final AttributeKey<String> PROXY_PROTOCOL_ADDR =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_ADDR);

    public static final AttributeKey<String> PROXY_PROTOCOL_PORT =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_PORT);

    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_ADDR =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_SERVER_ADDR);

    public static final AttributeKey<String> PROXY_PROTOCOL_SERVER_PORT =
            AttributeKey.valueOf(HAProxyConstants.PROXY_PROTOCOL_SERVER_PORT);

    private static final Map<String, AttributeKey<String>> ATTRIBUTE_KEY_MAP = new ConcurrentHashMap<>();

    public static AttributeKey<String> valueOf(String name) {
        return ATTRIBUTE_KEY_MAP.computeIfAbsent(name, AttributeKey::valueOf);
    }
}
