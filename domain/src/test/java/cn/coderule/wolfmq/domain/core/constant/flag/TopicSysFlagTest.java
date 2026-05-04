package cn.coderule.wolfmq.domain.core.constant.flag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TopicSysFlagTest {

    @Test
    void buildSysFlag_trueTrue() {
        int flag = TopicSysFlag.buildSysFlag(true, true);
        assertTrue(TopicSysFlag.hasUnitFlag(flag));
        assertTrue(TopicSysFlag.hasUnitSubFlag(flag));
    }

    @Test
    void buildSysFlag_trueFalse() {
        int flag = TopicSysFlag.buildSysFlag(true, false);
        assertTrue(TopicSysFlag.hasUnitFlag(flag));
        assertFalse(TopicSysFlag.hasUnitSubFlag(flag));
    }

    @Test
    void buildSysFlag_falseTrue() {
        int flag = TopicSysFlag.buildSysFlag(false, true);
        assertFalse(TopicSysFlag.hasUnitFlag(flag));
        assertTrue(TopicSysFlag.hasUnitSubFlag(flag));
    }

    @Test
    void buildSysFlag_falseFalse() {
        int flag = TopicSysFlag.buildSysFlag(false, false);
        assertFalse(TopicSysFlag.hasUnitFlag(flag));
        assertFalse(TopicSysFlag.hasUnitSubFlag(flag));
        assertEquals(0, flag);
    }

    @Test
    void setUnitFlag() {
        int flag = TopicSysFlag.buildSysFlag(false, true);
        int result = TopicSysFlag.setUnitFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void clearUnitFlag() {
        int flag = TopicSysFlag.buildSysFlag(true, true);
        int result = TopicSysFlag.clearUnitFlag(flag);
        assertFalse(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void setUnitSubFlag() {
        int flag = TopicSysFlag.buildSysFlag(true, false);
        int result = TopicSysFlag.setUnitSubFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void clearUnitSubFlag() {
        int flag = TopicSysFlag.buildSysFlag(true, true);
        int result = TopicSysFlag.clearUnitSubFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertFalse(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void setUnitFlag_doesNotAffectUnitSub() {
        int flag = TopicSysFlag.buildSysFlag(false, true);
        assertFalse(TopicSysFlag.hasUnitFlag(flag));
        assertTrue(TopicSysFlag.hasUnitSubFlag(flag));

        int result = TopicSysFlag.setUnitFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void clearUnitFlag_doesNotAffectUnitSub() {
        int flag = TopicSysFlag.buildSysFlag(true, true);
        int result = TopicSysFlag.clearUnitFlag(flag);
        assertFalse(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void setUnitSubFlag_doesNotAffectUnit() {
        int flag = TopicSysFlag.buildSysFlag(true, false);
        assertTrue(TopicSysFlag.hasUnitFlag(flag));
        assertFalse(TopicSysFlag.hasUnitSubFlag(flag));

        int result = TopicSysFlag.setUnitSubFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertTrue(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void clearUnitSubFlag_doesNotAffectUnit() {
        int flag = TopicSysFlag.buildSysFlag(true, true);
        int result = TopicSysFlag.clearUnitSubFlag(flag);
        assertTrue(TopicSysFlag.hasUnitFlag(result));
        assertFalse(TopicSysFlag.hasUnitSubFlag(result));
    }

    @Test
    void hasUnitFlag_onZero() {
        assertFalse(TopicSysFlag.hasUnitFlag(0));
    }

    @Test
    void hasUnitSubFlag_onZero() {
        assertFalse(TopicSysFlag.hasUnitSubFlag(0));
    }
}