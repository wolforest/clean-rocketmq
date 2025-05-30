
package cn.coderule.minimq.rpc.common.core.channel.remote;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.rpc.common.core.enums.ChannelProtocolType;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RemoteChannelSerializer {
    private static final String REMOTE_PROXY_IP_KEY = "remoteProxyIp";
    private static final String REMOTE_ADDRESS_KEY = "remoteAddress";
    private static final String LOCAL_ADDRESS_KEY = "localAddress";
    private static final String TYPE_KEY = "type";
    private static final String EXTEND_ATTRIBUTE_KEY = "extendAttribute";

    public static String toJson(RemoteChannel remoteChannel) {
        Map<String, Object> data = new HashMap<>();
        data.put(REMOTE_PROXY_IP_KEY, remoteChannel.getRemoteProxyIp());
        data.put(REMOTE_ADDRESS_KEY, remoteChannel.getRemoteAddress());
        data.put(LOCAL_ADDRESS_KEY, remoteChannel.getLocalAddress());
        data.put(TYPE_KEY, remoteChannel.getType());
        data.put(EXTEND_ATTRIBUTE_KEY, remoteChannel.getChannelExtendAttribute());
        return JSON.toJSONString(data);
    }

    public static RemoteChannel decodeFromJson(String jsonData) {
        if (StringUtil.isBlank(jsonData)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSON.parseObject(jsonData);
            return new RemoteChannel(
                jsonObject.getString(REMOTE_PROXY_IP_KEY),
                jsonObject.getString(REMOTE_ADDRESS_KEY),
                jsonObject.getString(LOCAL_ADDRESS_KEY),
                jsonObject.getObject(TYPE_KEY, ChannelProtocolType.class),
                jsonObject.getObject(EXTEND_ATTRIBUTE_KEY, String.class)
            );
        } catch (Throwable t) {
            log.error("decode remote channel data failed. data:{}", jsonData, t);
        }
        return null;
    }
}
