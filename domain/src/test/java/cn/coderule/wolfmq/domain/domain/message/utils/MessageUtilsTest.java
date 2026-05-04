package cn.coderule.wolfmq.domain.domain.message.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MessageUtilsTest {

    @Test
    void propertiesToString_and_back() {
        Map<String, String> props = Map.of("key1", "value1", "key2", "value2");
        String str = MessageUtils.propertiesToString(props);
        assertNotNull(str);

        Map<String, String> parsed = MessageUtils.stringToProperties(str);
        assertEquals("value1", parsed.get("key1"));
        assertEquals("value2", parsed.get("key2"));
    }

    @Test
    void propertiesToString_nullMap() {
        assertEquals("", MessageUtils.propertiesToString(null));
    }

    @Test
    void propertiesToString_emptyMap() {
        assertEquals("", MessageUtils.propertiesToString(Map.of()));
    }

    @Test
    void stringToProperties_nullInput() {
        Map<String, String> result = MessageUtils.stringToProperties(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void stringToProperties_emptyInput() {
        Map<String, String> result = MessageUtils.stringToProperties("");
        assertTrue(result.isEmpty());
    }

    @Test
    void stringToProperties_singleProperty() {
        String str = "key1" + MessageUtils.NAME_VALUE_SEPARATOR + "value1" + MessageUtils.PROPERTY_SEPARATOR;
        Map<String, String> result = MessageUtils.stringToProperties(str);
        assertEquals("value1", result.get("key1"));
    }

    @Test
    void stringToProperties_multipleProperties() {
        String str = "key1" + MessageUtils.NAME_VALUE_SEPARATOR + "value1" + MessageUtils.PROPERTY_SEPARATOR
            + "key2" + MessageUtils.NAME_VALUE_SEPARATOR + "value2" + MessageUtils.PROPERTY_SEPARATOR;
        Map<String, String> result = MessageUtils.stringToProperties(str);
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    void stringToProperties_skipsNullValue() {
        String str = "key1" + MessageUtils.NAME_VALUE_SEPARATOR + "v1" + MessageUtils.PROPERTY_SEPARATOR;
        Map<String, String> result = MessageUtils.stringToProperties(str);
        assertEquals(1, result.size());
    }

    @Test
    void getTagsCode_null() {
        assertEquals(0, MessageUtils.getTagsCode(null));
    }

    @Test
    void getTagsCode_empty() {
        assertEquals(0, MessageUtils.getTagsCode(""));
    }

    @Test
    void getTagsCode_nonEmpty() {
        long code = MessageUtils.getTagsCode("tagA");
        assertEquals("tagA".hashCode(), code);
    }

    @Test
    void getTagsCode_withTagType() {
        long code = MessageUtils.getTagsCode(cn.coderule.wolfmq.domain.core.enums.message.TagType.SINGLE_TAG, "tagA");
        assertEquals("tagA".hashCode(), code);
    }

    @Test
    void generateMd5_consistent() {
        String md5_1 = MessageUtils.generateMd5("hello world");
        String md5_2 = MessageUtils.generateMd5("hello world");
        assertEquals(md5_1, md5_2);
    }

    @Test
    void generateMd5_differentInputs() {
        String md5_1 = MessageUtils.generateMd5("hello");
        String md5_2 = MessageUtils.generateMd5("world");
        assertNotEquals(md5_1, md5_2);
    }

    @Test
    void string2bytes_roundtrip() {
        String hex = "0A1B2C3D";
        byte[] bytes = MessageUtils.string2bytes(hex);
        assertNotNull(bytes);
        assertEquals(4, bytes.length);
    }

    @Test
    void string2bytes_null() {
        assertNull(MessageUtils.string2bytes(null));
    }

    @Test
    void string2bytes_empty() {
        assertNull(MessageUtils.string2bytes(""));
    }
}