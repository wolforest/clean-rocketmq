package cn.coderule.wolfmq.domain.core.constant.flag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionSysFlagTest {

    @Test
    void buildSysFlag_true() {
        assertEquals(1, SubscriptionSysFlag.buildSysFlag(true));
    }

    @Test
    void buildSysFlag_false() {
        assertEquals(0, SubscriptionSysFlag.buildSysFlag(false));
    }

    @Test
    void setUnitFlag() {
        int flag = SubscriptionSysFlag.buildSysFlag(false);
        int result = SubscriptionSysFlag.setUnitFlag(flag);
        assertTrue(SubscriptionSysFlag.hasUnitFlag(result));
        assertEquals(1, result);
    }

    @Test
    void setUnitFlag_alreadySet() {
        int flag = SubscriptionSysFlag.buildSysFlag(true);
        int result = SubscriptionSysFlag.setUnitFlag(flag);
        assertTrue(SubscriptionSysFlag.hasUnitFlag(result));
        assertEquals(1, result);
    }

    @Test
    void clearUnitFlag() {
        int flag = SubscriptionSysFlag.buildSysFlag(true);
        int result = SubscriptionSysFlag.clearUnitFlag(flag);
        assertFalse(SubscriptionSysFlag.hasUnitFlag(result));
        assertEquals(0, result);
    }

    @Test
    void clearUnitFlag_alreadyCleared() {
        int flag = SubscriptionSysFlag.buildSysFlag(false);
        int result = SubscriptionSysFlag.clearUnitFlag(flag);
        assertFalse(SubscriptionSysFlag.hasUnitFlag(result));
        assertEquals(0, result);
    }

    @Test
    void hasUnitFlag_true() {
        int flag = SubscriptionSysFlag.buildSysFlag(true);
        assertTrue(SubscriptionSysFlag.hasUnitFlag(flag));
    }

    @Test
    void hasUnitFlag_false() {
        int flag = SubscriptionSysFlag.buildSysFlag(false);
        assertFalse(SubscriptionSysFlag.hasUnitFlag(flag));
    }
}