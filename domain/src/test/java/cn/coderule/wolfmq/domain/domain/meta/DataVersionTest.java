package cn.coderule.wolfmq.domain.domain.meta;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataVersionTest {

    @Test
    void nextVersion_incrementsCounter() {
        DataVersion v1 = new DataVersion();
        long counterBefore = v1.getCounter().get();
        v1.nextVersion();
        assertEquals(counterBefore + 1, v1.getCounter().get());
    }

    @Test
    void nextVersion_withStateVersion() {
        DataVersion v = new DataVersion();
        v.nextVersion(42L);
        assertEquals(42L, v.getStateVersion());
        assertEquals(1, v.getCounter().get());
    }

    @Test
    void nextVersion_updatesTimestamp() throws InterruptedException {
        DataVersion v = new DataVersion();
        long tsBefore = v.getTimestamp();
        Thread.sleep(10);
        v.nextVersion();
        assertTrue(v.getTimestamp() >= tsBefore);
    }

    @Test
    void assign_copiesValues() {
        DataVersion source = new DataVersion();
        source.nextVersion(42L);
        source.nextVersion(43L);

        DataVersion target = new DataVersion();
        target.assign(source);

        assertEquals(source.getTimestamp(), target.getTimestamp());
        assertEquals(source.getStateVersion(), target.getStateVersion());
        assertEquals(source.getCounter().get(), target.getCounter().get());
    }

    @Test
    void equals_sameValues() {
        DataVersion v1 = new DataVersion();
        DataVersion v2 = new DataVersion();
        assertEquals(v1, v2);
    }

    @Test
    void equals_differentAfterUpdate() {
        DataVersion v1 = new DataVersion();
        DataVersion v2 = new DataVersion();
        v1.nextVersion();
        assertNotEquals(v1, v2);
    }

    @Test
    void hashCode_consistentWithEquals() {
        DataVersion v1 = new DataVersion();
        DataVersion v2 = new DataVersion();
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test
    void initialState() {
        DataVersion v = new DataVersion();
        assertEquals(0L, v.getStateVersion());
        assertEquals(0, v.getCounter().get());
        assertTrue(v.getTimestamp() > 0);
    }
}