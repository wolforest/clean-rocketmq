
package cn.coderule.minimq.domain.core.attribute;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AttributeUtils {
    public static Map<String, String> alterCurrentAttributes(boolean create, Map<String, Attribute> all,
        ImmutableMap<String, String> currentAttributes, ImmutableMap<String, String> newAttributes) {

        Map<String, String> init = new HashMap<>();
        Map<String, String> add = new HashMap<>();
        Map<String, String> update = new HashMap<>();
        Map<String, String> delete = new HashMap<>();
        Set<String> keys = new HashSet<>();

        for (Map.Entry<String, String> attribute : newAttributes.entrySet()) {
            String key = attribute.getKey();
            String realKey = realKey(key);
            String value = attribute.getValue();

            validate(realKey);
            duplicationCheck(keys, realKey);

            if (create) {
                if (key.startsWith("+")) {
                    init.put(realKey, value);
                } else {
                    throw new RuntimeException("only add attribute is supported while creating topic. key: " + realKey);
                }
            } else {
                if (key.startsWith("+")) {
                    if (!currentAttributes.containsKey(realKey)) {
                        add.put(realKey, value);
                    } else {
                        update.put(realKey, value);
                    }
                } else if (key.startsWith("-")) {
                    if (!currentAttributes.containsKey(realKey)) {
                        throw new RuntimeException("attempt to delete a nonexistent key: " + realKey);
                    }
                    delete.put(realKey, value);
                } else {
                    throw new RuntimeException("wrong format key: " + realKey);
                }
            }
        }

        validateAlter(all, init, true, false);
        validateAlter(all, add, false, false);
        validateAlter(all, update, false, false);
        validateAlter(all, delete, false, true);

        log.info("add: {}, update: {}, delete: {}", add, update, delete);
        HashMap<String, String> finalAttributes = new HashMap<>(currentAttributes);
        finalAttributes.putAll(init);
        finalAttributes.putAll(add);
        finalAttributes.putAll(update);
        for (String s : delete.keySet()) {
            finalAttributes.remove(s);
        }
        return finalAttributes;
    }

    private static void duplicationCheck(Set<String> keys, String key) {
        boolean notExist = keys.add(key);
        if (!notExist) {
            throw new RuntimeException("alter duplication key. key: " + key);
        }
    }

    private static void validate(String kvAttribute) {
        if (Strings.isNullOrEmpty(kvAttribute)) {
            throw new RuntimeException("kv string format wrong.");
        }

        if (kvAttribute.contains("+")) {
            throw new RuntimeException("kv string format wrong.");
        }

        if (kvAttribute.contains("-")) {
            throw new RuntimeException("kv string format wrong.");
        }
    }

    private static void validateAlter(Map<String, Attribute> all, Map<String, String> alter, boolean init, boolean delete) {
        for (Map.Entry<String, String> entry : alter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            Attribute attribute = all.get(key);
            if (attribute == null) {
                throw new RuntimeException("unsupported key: " + key);
            }
            if (!init && !attribute.isChangeable()) {
                throw new RuntimeException("attempt to update an unchangeable attribute. key: " + key);
            }

            if (!delete) {
                attribute.verify(value);
            }
        }
    }

    private static String realKey(String key) {
        return key.substring(1);
    }
}
