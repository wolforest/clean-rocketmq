package cn.coderule.minimq.domain.core.attribute;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeParser {

    public static final String ATTR_ARRAY_SEPARATOR_COMMA = ",";

    public static final String ATTR_KEY_VALUE_EQUAL_SIGN = "=";

    public static final String ATTR_ADD_PLUS_SIGN = "+";

    private static final String ATTR_DELETE_MINUS_SIGN = "-";

    public static Map<String, String> toMap(String attributesModification) {
        if (Strings.isNullOrEmpty(attributesModification)) {
            return new HashMap<>();
        }

        // format: +key1=value1,+key2=value2,-key3,+key4=value4
        Map<String, String> attributes = new HashMap<>();
        String[] kvs = attributesModification.split(ATTR_ARRAY_SEPARATOR_COMMA);
        for (String kv : kvs) {
            String key;
            String value;
            if (kv.contains(ATTR_KEY_VALUE_EQUAL_SIGN)) {
                String[] splits = kv.split(ATTR_KEY_VALUE_EQUAL_SIGN);
                key = splits[0];
                value = splits[1];
                if (!key.contains(ATTR_ADD_PLUS_SIGN)) {
                    throw new RuntimeException("add/alter attribute format is wrong: " + key);
                }
            } else {
                key = kv;
                value = "";
                if (!key.contains(ATTR_DELETE_MINUS_SIGN)) {
                    throw new RuntimeException("delete attribute format is wrong: " + key);
                }
            }
            String old = attributes.put(key, value);
            if (old != null) {
                throw new RuntimeException("key duplication: " + key);
            }
        }
        return attributes;
    }

    public static String toString(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }

        List<String> kvs = new ArrayList<>();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {

            String value = entry.getValue();
            if (Strings.isNullOrEmpty(value)) {
                kvs.add(entry.getKey());
            } else {
                kvs.add(entry.getKey() + ATTR_KEY_VALUE_EQUAL_SIGN + entry.getValue());
            }
        }
        return String.join(ATTR_ARRAY_SEPARATOR_COMMA, kvs);
    }
}
