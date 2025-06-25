package cn.coderule.minimq.domain.utils;

import cn.coderule.common.util.lang.string.StringUtil;
import cn.coderule.minimq.domain.core.enums.message.TagType;
import java.util.HashMap;
import java.util.Map;

public class MessageUtils {
    public static final char NAME_VALUE_SEPARATOR = 1;
    public static final char PROPERTY_SEPARATOR = 2;

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

}
