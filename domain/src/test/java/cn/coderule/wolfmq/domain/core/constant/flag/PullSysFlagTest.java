package cn.coderule.wolfmq.domain.core.constant.flag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PullSysFlagTest {

    @Test
    void buildSysFlag_allFalse() {
        assertEquals(0, PullSysFlag.buildSysFlag(false, false, false, false));
    }

    @Test
    void buildSysFlag_allTrue() {
        int flag = PullSysFlag.buildSysFlag(true, true, true, true);
        assertTrue(PullSysFlag.hasCommitOffsetFlag(flag));
        assertTrue(PullSysFlag.hasSuspendFlag(flag));
        assertTrue(PullSysFlag.hasSubscriptionFlag(flag));
        assertTrue(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_commitOffsetOnly() {
        int flag = PullSysFlag.buildSysFlag(true, false, false, false);
        assertTrue(PullSysFlag.hasCommitOffsetFlag(flag));
        assertFalse(PullSysFlag.hasSuspendFlag(flag));
        assertFalse(PullSysFlag.hasSubscriptionFlag(flag));
        assertFalse(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_suspendOnly() {
        int flag = PullSysFlag.buildSysFlag(false, true, false, false);
        assertFalse(PullSysFlag.hasCommitOffsetFlag(flag));
        assertTrue(PullSysFlag.hasSuspendFlag(flag));
        assertFalse(PullSysFlag.hasSubscriptionFlag(flag));
        assertFalse(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_subscriptionOnly() {
        int flag = PullSysFlag.buildSysFlag(false, false, true, false);
        assertFalse(PullSysFlag.hasCommitOffsetFlag(flag));
        assertFalse(PullSysFlag.hasSuspendFlag(flag));
        assertTrue(PullSysFlag.hasSubscriptionFlag(flag));
        assertFalse(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_classFilterOnly() {
        int flag = PullSysFlag.buildSysFlag(false, false, false, true);
        assertFalse(PullSysFlag.hasCommitOffsetFlag(flag));
        assertFalse(PullSysFlag.hasSuspendFlag(flag));
        assertFalse(PullSysFlag.hasSubscriptionFlag(flag));
        assertTrue(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_commitAndSuspend() {
        int flag = PullSysFlag.buildSysFlag(true, true, false, false);
        assertTrue(PullSysFlag.hasCommitOffsetFlag(flag));
        assertTrue(PullSysFlag.hasSuspendFlag(flag));
        assertFalse(PullSysFlag.hasSubscriptionFlag(flag));
        assertFalse(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void clearCommitOffsetFlag() {
        int flag = PullSysFlag.buildSysFlag(true, true, true, true);
        int cleared = PullSysFlag.clearCommitOffsetFlag(flag);
        assertFalse(PullSysFlag.hasCommitOffsetFlag(cleared));
        assertTrue(PullSysFlag.hasSuspendFlag(cleared));
        assertTrue(PullSysFlag.hasSubscriptionFlag(cleared));
        assertTrue(PullSysFlag.hasClassFilterFlag(cleared));
    }

    @Test
    void clearSuspendFlag() {
        int flag = PullSysFlag.buildSysFlag(true, true, true, true);
        int cleared = PullSysFlag.clearSuspendFlag(flag);
        assertTrue(PullSysFlag.hasCommitOffsetFlag(cleared));
        assertFalse(PullSysFlag.hasSuspendFlag(cleared));
        assertTrue(PullSysFlag.hasSubscriptionFlag(cleared));
        assertTrue(PullSysFlag.hasClassFilterFlag(cleared));
    }

    @Test
    void buildSysFlagWithSubscription() {
        int flag = PullSysFlag.buildSysFlag(true, false, false, false);
        int withSub = PullSysFlag.buildSysFlagWithSubscription(flag);
        assertTrue(PullSysFlag.hasSubscriptionFlag(withSub));
        assertTrue(PullSysFlag.hasCommitOffsetFlag(withSub));
    }

    @Test
    void buildSysFlagWithSubscription_alreadyHasSubscription() {
        int flag = PullSysFlag.buildSysFlag(true, false, true, false);
        int withSub = PullSysFlag.buildSysFlagWithSubscription(flag);
        assertTrue(PullSysFlag.hasSubscriptionFlag(withSub));
        assertTrue(PullSysFlag.hasCommitOffsetFlag(withSub));
    }

    @Test
    void buildSysFlag_fiveArg_includesLitePull() {
        int flag = PullSysFlag.buildSysFlag(true, true, true, true, true);
        assertTrue(PullSysFlag.hasLitePullFlag(flag));
        assertTrue(PullSysFlag.hasCommitOffsetFlag(flag));
        assertTrue(PullSysFlag.hasSuspendFlag(flag));
        assertTrue(PullSysFlag.hasSubscriptionFlag(flag));
        assertTrue(PullSysFlag.hasClassFilterFlag(flag));
    }

    @Test
    void buildSysFlag_fiveArg_litePullFalse() {
        int flag = PullSysFlag.buildSysFlag(true, true, true, true, false);
        assertFalse(PullSysFlag.hasLitePullFlag(flag));
    }

    @Test
    void hasLitePullFlag_falseOnZero() {
        assertFalse(PullSysFlag.hasLitePullFlag(0));
    }

    @Test
    void hasCommitOffsetFlag_falseOnZero() {
        assertFalse(PullSysFlag.hasCommitOffsetFlag(0));
    }

    @Test
    void hasSuspendFlag_falseOnZero() {
        assertFalse(PullSysFlag.hasSuspendFlag(0));
    }

    @Test
    void hasSubscriptionFlag_falseOnZero() {
        assertFalse(PullSysFlag.hasSubscriptionFlag(0));
    }

    @Test
    void hasClassFilterFlag_falseOnZero() {
        assertFalse(PullSysFlag.hasClassFilterFlag(0));
    }
}