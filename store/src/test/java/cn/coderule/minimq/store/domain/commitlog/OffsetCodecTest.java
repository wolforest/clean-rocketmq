package cn.coderule.minimq.store.domain.commitlog;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffsetCodecTest {

    private static final int MAX_SHARDING = 100;

    @Test
    void testEncode_ZeroOffset() {
        OffsetCodec codec = new OffsetCodec(1, MAX_SHARDING);
        assertEquals(1, codec.encode(0));
    }

    @Test
    void testEncode_PositiveOffset() {
        OffsetCodec codec = new OffsetCodec(2, MAX_SHARDING);
        assertEquals(502, codec.encode(5));
    }

    @Test
    void testEncode_NegativeOffset() {
        OffsetCodec codec = new OffsetCodec(1, MAX_SHARDING);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> codec.encode(-1)
        );
        assertEquals("offset can't be negative", ex.getMessage());
    }

    @Test
    void testDecode_ZeroOffset() {
        OffsetCodec codec = new OffsetCodec(1, MAX_SHARDING);
        assertEquals(0, codec.decode(1));
    }

    @Test
    void testDecode_PositiveOffset() {
        OffsetCodec codec = new OffsetCodec(2, MAX_SHARDING);
        assertEquals(0, codec.decode(2));
    }

    @Test
    void testDecode_NegativeOffset() {
        OffsetCodec codec = new OffsetCodec(1, MAX_SHARDING);
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> codec.decode(-1)
        );
        assertEquals("offset can't be negative", ex.getMessage());
    }

    @Test
    void testEncodeDecode_RoundTrip() {
        OffsetCodec codec = new OffsetCodec(3, MAX_SHARDING);
        long originalOffset = 12345L;

        long encoded = codec.encode(originalOffset);
        long decoded = codec.decode(encoded);

        assertEquals(originalOffset, decoded);
    }

    @Test
    void testEncodeDecode_MultipleOffsets() {
        OffsetCodec codec = new OffsetCodec(2, MAX_SHARDING);
        long[] offsets = {0, 1, 10, 100, 1000, 999999};

        for (long offset : offsets) {
            long encoded = codec.encode(offset);
            long decoded = codec.decode(encoded);
            assertEquals(offset, decoded, "Round trip failed for offset: " + offset);
        }
    }

    @Test
    void testEncodeDecode_MultipleShards() {
        long originalOffset = 100;

        for (int shardId = 0; shardId < MAX_SHARDING; shardId++) {
            OffsetCodec codec = new OffsetCodec(shardId, MAX_SHARDING);

            long encoded = codec.encode(originalOffset);
            long decoded = codec.decode(encoded);

            assertEquals(originalOffset, decoded,
                "Round trip failed for shardId: " + shardId);
        }
    }

    @Test
    void testEncode_LargeOffset() {
        OffsetCodec codec = new OffsetCodec(1, MAX_SHARDING);
        long largeOffset = Long.MAX_VALUE / 100;

        long encoded = codec.encode(largeOffset);
        assertTrue(encoded > largeOffset);
        assertEquals(largeOffset, codec.decode(encoded));
    }

    @Test
    void testEncode_ShardIdZero() {
        OffsetCodec codec = new OffsetCodec(0, MAX_SHARDING);
        assertEquals(0, codec.encode(0));
        assertEquals(1000, codec.encode(10));
    }

    @Test
    void testEncode_ShardIdMax() {
        OffsetCodec codec = new OffsetCodec(MAX_SHARDING - 1, MAX_SHARDING);
        assertEquals(99, codec.encode(0));
        assertEquals(1099, codec.encode(10));
    }

    @Test
    void testDecode_EncodedByDifferentShard() {
        long offset = 50;

        OffsetCodec codec0 = new OffsetCodec(0, MAX_SHARDING);
        OffsetCodec codec1 = new OffsetCodec(1, MAX_SHARDING);
        OffsetCodec codec2 = new OffsetCodec(2, MAX_SHARDING);
        OffsetCodec codec3 = new OffsetCodec(3, MAX_SHARDING);
        OffsetCodec codec99 = new OffsetCodec(99, MAX_SHARDING);

        assertEquals(5000, codec0.encode(offset));
        assertEquals(5001, codec1.encode(offset));
        assertEquals(5002, codec2.encode(offset));
        assertEquals(5003, codec3.encode(offset));
        assertEquals(5099, codec99.encode(offset));

        long encoded0 = codec0.encode(offset);
        long encoded1 = codec1.encode(offset);
        long encoded2 = codec2.encode(offset);
        long encoded3 = codec3.encode(offset);
        long encoded99 = codec99.encode(offset);

        assertEquals(offset, codec0.decode(encoded0));
        assertEquals(offset, codec1.decode(encoded1));
        assertEquals(offset, codec2.decode(encoded2));
        assertEquals(offset, codec3.decode(encoded3));
        assertEquals(offset, codec99.decode(encoded99));
    }

    @Test
    void testEncodeDecode_SameOffsetDifferentShards() {
        long offset = 7;

        long[] encoded = new long[MAX_SHARDING];
        for (int i = 0; i < MAX_SHARDING; i++) {
            OffsetCodec codec = new OffsetCodec(i, MAX_SHARDING);
            encoded[i] = codec.encode(offset);
        }

        for (int i = 0; i < MAX_SHARDING; i++) {
            for (int j = i + 1; j < MAX_SHARDING; j++) {
                assertNotEquals(encoded[i], encoded[j],
                    "Different shards should produce different encoded values");
            }
        }
    }

    @Test
    void testConstructor() {
        OffsetCodec codec = new OffsetCodec(2, 8);
        assertEquals(0, codec.decode(codec.encode(0)));
        assertEquals(100, codec.decode(codec.encode(100)));
    }
}
