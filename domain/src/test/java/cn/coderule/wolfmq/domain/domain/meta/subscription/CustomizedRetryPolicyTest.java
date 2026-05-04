package cn.coderule.wolfmq.domain.domain.meta.subscription;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CustomizedRetryPolicyTest {

    @Test
    void nextDelayDuration_reconsumeTimes0_returnsIndex2() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(levels[2], policy.nextDelayDuration(0));
    }

    @Test
    void nextDelayDuration_reconsumeTimes2_returnsIndex4() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(levels[4], policy.nextDelayDuration(2));
    }

    @Test
    void nextDelayDuration_reconsumeTimes8_returnsIndex10() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(levels[10], policy.nextDelayDuration(8));
    }

    @Test
    void nextDelayDuration_exceedsArray_clampsToLastIndex() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(levels[levels.length - 1], policy.nextDelayDuration(100));
    }

    @Test
    void nextDelayDuration_negativeTreatedAsZero() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(levels[2], policy.nextDelayDuration(-1));
    }

    @Test
    void customLevels_withSmallArray_clampsToLast() {
        long[] custom = {1000L, 2000L, 3000L, 5000L};
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy(custom);
        assertEquals(custom[2], policy.nextDelayDuration(0));
        assertEquals(custom[3], policy.nextDelayDuration(1));
        assertEquals(custom[3], policy.nextDelayDuration(10));
    }

    @Test
    void defaultLevels_correctTotalCount() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        assertEquals(18, policy.getNext().length);
    }

    @Test
    void defaultLevels_boundaryValues() {
        CustomizedRetryPolicy policy = new CustomizedRetryPolicy();
        long[] levels = policy.getNext();
        assertEquals(TimeUnit.SECONDS.toMillis(1), levels[0]);
        assertEquals(TimeUnit.HOURS.toMillis(2), levels[levels.length - 1]);
    }
}