package cn.coderule.wolfmq.domain.core.attribute;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AttributeParserTest {

    @Test
    void toMap_addAttributes() {
        Map<String, String> result = AttributeParser.toMap("+key1=value1,+key2=value2");
        assertEquals(2, result.size());
        assertEquals("value1", result.get("+key1"));
        assertEquals("value2", result.get("+key2"));
    }

    @Test
    void toMap_deleteAttributes() {
        Map<String, String> result = AttributeParser.toMap("-key1,-key2");
        assertEquals(2, result.size());
        assertEquals("", result.get("-key1"));
        assertEquals("", result.get("-key2"));
    }

    @Test
    void toMap_mixedAddAndDelete() {
        Map<String, String> result = AttributeParser.toMap("+key1=value1,-key2");
        assertEquals(2, result.size());
        assertEquals("value1", result.get("+key1"));
        assertEquals("", result.get("-key2"));
    }

    @Test
    void toMap_emptyInput() {
        Map<String, String> result = AttributeParser.toMap("");
        assertTrue(result.isEmpty());
    }

    @Test
    void toMap_nullInput() {
        Map<String, String> result = AttributeParser.toMap(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void toMap_addWithoutPlusThrows() {
        assertThrows(RuntimeException.class, () -> AttributeParser.toMap("key1=value1"));
    }

    @Test
    void toMap_deleteWithoutMinusThrows() {
        assertThrows(RuntimeException.class, () -> AttributeParser.toMap("key1"));
    }

    @Test
    void toMap_duplicateKeyThrows() {
        assertThrows(RuntimeException.class, () -> AttributeParser.toMap("+key1=value1,+key1=value2"));
    }

    @Test
    void toString_normalMap() {
        Map<String, String> map = Map.of("+key1", "value1", "+key2", "value2");
        String result = AttributeParser.toString(map);
        assertTrue(result.contains("+key1=value1"));
        assertTrue(result.contains("+key2=value2"));
        assertTrue(result.contains(","));
    }

    @Test
    void toString_emptyMap() {
        String result = AttributeParser.toString(Map.of());
        assertEquals("", result);
    }

    @Test
    void toString_nullMap() {
        String result = AttributeParser.toString(null);
        assertEquals("", result);
    }

    @Test
    void toString_deleteAttributeNoValue() {
        Map<String, String> map = Map.of("-key1", "");
        String result = AttributeParser.toString(map);
        assertTrue(result.contains("-key1"));
    }
}