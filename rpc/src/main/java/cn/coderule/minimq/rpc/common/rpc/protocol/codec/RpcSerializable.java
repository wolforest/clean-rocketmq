package cn.coderule.minimq.rpc.common.rpc.protocol.codec;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public abstract class RpcSerializable implements Serializable {
    private final static Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    public static byte[] encode(final Object obj) {
        if (obj == null) {
            return null;
        }
        final String json = toJson(obj);
        return json.getBytes(CHARSET_UTF8);
    }

    public static byte[] encode(final Object obj, JSONWriter.Feature... features) {
        if (obj == null) {
            return null;
        }

        String json = JSON.toJSONString(obj, features);
        return json.getBytes(CHARSET_UTF8);
    }

    public static String toJson(final Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T decode(final byte[] data, Class<T> classOfT) {
        if (data == null) {
            return null;
        }
        return fromJson(data, classOfT);
    }

    public static <T> List<T> decodeList(final byte[] data, Class<T> classOfT) {
        if (data == null) {
            return null;
        }
        String json = new String(data, CHARSET_UTF8);
        return JSON.parseArray(json, classOfT);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return JSON.parseObject(json, classOfT);
    }

    private static <T> T fromJson(byte[] data, Class<T> classOfT) {
        return JSON.parseObject(data, classOfT);
    }

    public byte[] encode(JSONWriter.Feature... features) {
        String json = JSON.toJSONString(this, features);
        return json.getBytes(CHARSET_UTF8);
    }

    public byte[] encode() {
        final String json = this.toJson();
        if (json != null) {
            return json.getBytes(CHARSET_UTF8);
        }
        return null;
    }

    public String toJson() {
        return toJson(this);
    }
}
