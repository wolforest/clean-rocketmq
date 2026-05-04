package cn.coderule.wolfmq.domain.core.attribute;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeUtilsTest {

    private Map<String, Attribute> createAttributes() {
        Map<String, Attribute> attrs = new HashMap<>();
        attrs.put("maxMsgSize", new LongRangeAttribute("maxMsgSize", true, 1, 100000, 4096));
        attrs.put("enable", new BooleanAttribute("enable", true, false));
        attrs.put("level", new EnumAttribute("level", true, java.util.Set.of("low", "medium", "high"), "medium"));
        return attrs;
    }

    @Test
    void alterCurrentAttributes_createNew() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+maxMsgSize", "8192", "+enable", "true");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs);
        assertEquals("8192", result.get("maxMsgSize"));
        assertEquals("true", result.get("enable"));
    }

    @Test
    void alterCurrentAttributes_addExistingKey_updates() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of("maxMsgSize", "4096");
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+maxMsgSize", "8192");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(false, all, current, newAttrs);
        assertEquals("8192", result.get("maxMsgSize"));
    }

    @Test
    void alterCurrentAttributes_updateExisting() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of("maxMsgSize", "4096");
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+maxMsgSize", "8192");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(false, all, current, newAttrs);
        assertEquals("8192", result.get("maxMsgSize"));
    }

    @Test
    void alterCurrentAttributes_deleteExisting() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of("maxMsgSize", "4096");
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("-maxMsgSize", "");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(false, all, current, newAttrs);
        assertNull(result.get("maxMsgSize"));
    }

    @Test
    void alterCurrentAttributes_deleteNonExistent_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("-maxMsgSize", "");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(false, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_invalidValue_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+maxMsgSize", "999999");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_unsupportedKey_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+unknown", "value");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_createOnlyAllowsPlusPrefix() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("maxMsgSize", "8192");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_enumAttribute_validValue() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+level", "high");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs);
        assertEquals("high", result.get("level"));
    }

    @Test
    void alterCurrentAttributes_enumAttribute_invalidValue_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+level", "invalid");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_duplicateKey_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        // ImmutableMap rejects duplicate keys at construction time
        assertThrows(IllegalArgumentException.class,
            () -> ImmutableMap.of("+maxMsgSize", "8192", "+maxMsgSize", "16384"));
    }

    @Test
    void alterCurrentAttributes_unchangeableAttribute_throwsOnUpdate() {
        Map<String, Attribute> all = new HashMap<>();
        all.put("fixed", new LongRangeAttribute("fixed", false, 1, 100, 50));
        ImmutableMap<String, String> current = ImmutableMap.of("fixed", "50");
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+fixed", "80");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(false, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_booleanAttribute_invalidValue_throws() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+enable", "maybe");

        assertThrows(RuntimeException.class,
            () -> AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs));
    }

    @Test
    void alterCurrentAttributes_booleanAttribute_validTrue() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+enable", "true");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs);
        assertEquals("true", result.get("enable"));
    }

    @Test
    void alterCurrentAttributes_booleanAttribute_validFalse() {
        Map<String, Attribute> all = createAttributes();
        ImmutableMap<String, String> current = ImmutableMap.of();
        ImmutableMap<String, String> newAttrs = ImmutableMap.of("+enable", "false");

        Map<String, String> result = AttributeUtils.alterCurrentAttributes(true, all, current, newAttrs);
        assertEquals("false", result.get("enable"));
    }
}