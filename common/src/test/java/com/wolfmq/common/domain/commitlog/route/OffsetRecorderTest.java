package com.wolfmq.common.domain.commitlog.route;

import com.wolfmq.common.config.store.CommitConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OffsetRecorderTest {
    private static final int SHARDING_NUMBER = 4;
    private static final int MAX_SHARDING_NUMBER = 100;

    private OffsetRecorder recorder() {
        CommitConfig config = new CommitConfig();
        config.setShardingNumber(SHARDING_NUMBER);
        config.setMaxShardingNumber(MAX_SHARDING_NUMBER);
        return new OffsetRecorder(config);
    }

    private long offset(int shardId, long value) {
        return new OffsetCodec(shardId, MAX_SHARDING_NUMBER).encode(value);
    }

    @Test
    void initializedOffsetsAreZero() {
        OffsetRecorder recorder = recorder();

        assertFalse(recorder.belowMinOffset(offset(0, 0)));
        assertFalse(recorder.exceedsMaxOffset(offset(0, 0)));
        assertTrue(recorder.exceedsMaxOffset(offset(0, 1)));
    }

    @Test
    void setMaxOffset() {
        OffsetRecorder recorder = recorder();
        recorder.setMaxOffset(offset(1, 10));

        assertFalse(recorder.exceedsMaxOffset(offset(1, 10)));
        assertTrue(recorder.exceedsMaxOffset(offset(1, 11)));
        assertFalse(recorder.exceedsMaxOffset(offset(1, 9)));
    }

    @Test
    void setMinOffset() {
        OffsetRecorder recorder = recorder();
        recorder.setMinOffset(offset(2, 5));

        // Min offset starts at 0 and setMinOffset only lowers it, so a
        // positive offset cannot raise the minimum.
        assertFalse(recorder.belowMinOffset(offset(2, 0)));
        assertFalse(recorder.belowMinOffset(offset(2, 4)));
        assertFalse(recorder.belowMinOffset(offset(2, 6)));
    }

    @Test
    void shardsAreIndependent() {
        OffsetRecorder recorder = recorder();
        recorder.setMaxOffset(offset(0, 10));
        recorder.setMaxOffset(offset(1, 20));

        assertFalse(recorder.exceedsMaxOffset(offset(0, 9)));
        assertTrue(recorder.exceedsMaxOffset(offset(0, 11)));

        assertFalse(recorder.exceedsMaxOffset(offset(1, 19)));
        assertTrue(recorder.exceedsMaxOffset(offset(1, 21)));
    }

    @Test
    void minAndMaxAreTrackedSeparately() {
        OffsetRecorder recorder = recorder();
        recorder.setMinOffset(offset(3, 5));
        recorder.setMaxOffset(offset(3, 10));

        assertFalse(recorder.belowMinOffset(offset(3, 0)));
        assertFalse(recorder.belowMinOffset(offset(3, 6)));

        assertFalse(recorder.exceedsMaxOffset(offset(3, 9)));
        assertTrue(recorder.exceedsMaxOffset(offset(3, 11)));
    }
}
