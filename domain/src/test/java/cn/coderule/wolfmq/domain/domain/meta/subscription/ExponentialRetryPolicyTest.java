package cn.coderule.wolfmq.domain.domain.meta.subscription;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExponentialRetryPolicyTest {

    @Test
    void nextDelayDuration_zeroTimes_returnsInitial() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy();
        assertEquals(5000L, policy.nextDelayDuration(0));
    }

    @Test
    void nextDelayDuration_oneTime_doubles() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy();
        assertEquals(10000L, policy.nextDelayDuration(1));
    }

    @Test
    void nextDelayDuration_cappedAtMax() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy(1000L, 60000L, 2);
        long result = policy.nextDelayDuration(100);
        assertEquals(60000L, result);
    }

    @Test
    void nextDelayDuration_negativeTreatedAsZero() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy(5000L, 7200000L, 2);
        assertEquals(5000L, policy.nextDelayDuration(-1));
    }

    @Test
    void nextDelayDuration_times32_capped() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy(5000L, 7200000L, 2);
        long result = policy.nextDelayDuration(33);
        long result32 = policy.nextDelayDuration(32);
        assertEquals(Math.min(7200000L, result32), result);
    }

    @Test
    void customParameters() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy(1000L, 30000L, 3);
        assertEquals(1000L, policy.nextDelayDuration(0));
        assertEquals(3000L, policy.nextDelayDuration(1));
        assertEquals(9000L, policy.nextDelayDuration(2));
        assertEquals(27000L, policy.nextDelayDuration(3));
        assertEquals(30000L, policy.nextDelayDuration(4));
    }

    @Test
    void settersWork() {
        ExponentialRetryPolicy policy = new ExponentialRetryPolicy();
        policy.setInitial(100L);
        policy.setMax(10000L);
        policy.setMultiplier(10);
        assertEquals(100L, policy.getInitial());
        assertEquals(10000L, policy.getMax());
        assertEquals(10L, policy.getMultiplier());
    }
}