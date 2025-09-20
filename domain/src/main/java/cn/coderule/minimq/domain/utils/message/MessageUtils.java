package cn.coderule.minimq.domain.utils.message;

import cn.coderule.common.util.encrypt.HashUtil;
import cn.coderule.common.util.lang.ByteUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import cn.coderule.minimq.domain.domain.message.MessageId;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    public static final char NAME_VALUE_SEPARATOR = 1;
    public static final char PROPERTY_SEPARATOR = 2;

    public static byte[] calculateMd5(byte[] binaryData) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found.");
        }
        messageDigest.update(binaryData);
        return messageDigest.digest();
    }

    public static String generateMd5(String bodyStr) {
        byte[] bytes = calculateMd5(bodyStr.getBytes(StandardCharsets.UTF_8));
        return ByteUtil.encodeHexString(bytes, false);
    }

    public static String generateMd5(byte[] content) {
        byte[] bytes = calculateMd5(content);
        return ByteUtil.encodeHexString(bytes, false);
    }

    public static long getTagsCode(String tags) {
        if (StringUtil.isBlank(tags)) {
            return 0;
        }

        return tags.hashCode();
    }

    public static long getTagsCode(TagType tagType, String tags) {
        return getTagsCode(tags);
    }

    public static String propertiesToString(Map<String, String> properties) {
        if (properties == null) {
            return "";
        }

        int len = 0;
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (name != null) {
                len += name.length();
            }
            len += value.length();
            len += 2; // separator
        }

        StringBuilder sb = new StringBuilder(len);
        for (final Map.Entry<String, String> entry : properties.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();

            if (value == null) {
                continue;
            }
            sb.append(name);
            sb.append(NAME_VALUE_SEPARATOR);
            sb.append(value);
            sb.append(PROPERTY_SEPARATOR);
        }
        return sb.toString();
    }

    public static Map<String, String> stringToProperties(final String properties) {
        Map<String, String> map = new HashMap<>(128);
        if (StringUtil.isBlank(properties)) {
            return map;
        }

        int len = properties.length();
        int index = 0;
        while (index < len) {
            int newIndex = properties.indexOf(PROPERTY_SEPARATOR, index);
            if (newIndex < 0) {
                newIndex = len;
            }
            if (newIndex - index >= 3) {
                int kvSepIndex = properties.indexOf(NAME_VALUE_SEPARATOR, index);
                if (kvSepIndex > index && kvSepIndex < newIndex - 1) {
                    String k = properties.substring(index, kvSepIndex);
                    String v = properties.substring(kvSepIndex + 1, newIndex);
                    map.put(k, v);
                }
            }
            index = newIndex + 1;
        }

        return map;
    }

    public static ByteBuffer socketAddress2ByteBuffer(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        InetAddress address = inetSocketAddress.getAddress();
        ByteBuffer byteBuffer;
        if (address instanceof Inet4Address) {
            byteBuffer = ByteBuffer.allocate(4 + 4);
        } else {
            byteBuffer = ByteBuffer.allocate(16 + 4);
        }
        return socketAddress2ByteBuffer(socketAddress, byteBuffer);
    }

    public static ByteBuffer socketAddress2ByteBuffer(final SocketAddress socketAddress, final ByteBuffer byteBuffer) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        InetAddress address = inetSocketAddress.getAddress();
        if (address instanceof Inet4Address) {
            byteBuffer.put(inetSocketAddress.getAddress().getAddress(), 0, 4);
        } else {
            byteBuffer.put(inetSocketAddress.getAddress().getAddress(), 0, 16);
        }
        byteBuffer.putInt(inetSocketAddress.getPort());
        byteBuffer.flip();
        return byteBuffer;
    }

    public static MessageId decodeMessageId(final String msgId) throws UnknownHostException {
        byte[] bytes = string2bytes(msgId);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        // address(ip+port)
        byte[] ip = new byte[msgId.length() == 32 ? 4 : 16];
        byteBuffer.get(ip);
        int port = byteBuffer.getInt();
        SocketAddress address = new InetSocketAddress(InetAddress.getByAddress(ip), port);

        // offset
        long offset = byteBuffer.getLong();

        return new MessageId(address, offset);
    }

    public static byte[] string2bytes(String hexString) {
        if (hexString == null || hexString.isEmpty()) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



}
