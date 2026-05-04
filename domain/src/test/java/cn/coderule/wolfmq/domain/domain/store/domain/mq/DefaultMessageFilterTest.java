package cn.coderule.wolfmq.domain.domain.store.domain.mq;

import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultMessageFilterTest {

    @Test
    void isMatchedByConsumeQueue_nullTagsCode_returnsTrue() {
        SubscriptionData subData = new SubscriptionData("topic", "*");
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertTrue(filter.isMatchedByConsumeQueue(null, null));
    }

    @Test
    void isMatchedByConsumeQueue_nullSubscriptionData_returnsTrue() {
        DefaultMessageFilter filter = new DefaultMessageFilter(null);

        assertTrue(filter.isMatchedByConsumeQueue(1L, null));
    }

    @Test
    void isMatchedByConsumeQueue_classFilterMode_returnsTrue() {
        SubscriptionData subData = new SubscriptionData("topic", "tag1");
        subData.setClassFilterMode(true);
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertTrue(filter.isMatchedByConsumeQueue(999L, null));
    }

    @Test
    void isMatchedByConsumeQueue_subAll_returnsTrue() {
        SubscriptionData subData = new SubscriptionData("topic", SubscriptionData.SUB_ALL);
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertTrue(filter.isMatchedByConsumeQueue(1L, null));
    }

    @Test
    void isMatchedByConsumeQueue_matchingTagsCode_returnsTrue() {
        SubscriptionData subData = new SubscriptionData("topic", "tag1");
        Set<Integer> codeSet = new HashSet<>();
        codeSet.add(1);
        codeSet.add(2);
        codeSet.add(3);
        subData.setCodeSet(codeSet);
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertTrue(filter.isMatchedByConsumeQueue(1L, null));
        assertTrue(filter.isMatchedByConsumeQueue(2L, null));
        assertTrue(filter.isMatchedByConsumeQueue(3L, null));
    }

    @Test
    void isMatchedByConsumeQueue_nonMatchingTagsCode_returnsFalse() {
        SubscriptionData subData = new SubscriptionData("topic", "tag1");
        Set<Integer> codeSet = new HashSet<>();
        codeSet.add(1);
        codeSet.add(2);
        codeSet.add(3);
        subData.setCodeSet(codeSet);
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertFalse(filter.isMatchedByConsumeQueue(99L, null));
        assertFalse(filter.isMatchedByConsumeQueue(0L, null));
    }

    @Test
    void isMatchedByCommitLog_alwaysReturnsTrue() {
        SubscriptionData subData = new SubscriptionData("topic", "*");
        DefaultMessageFilter filter = new DefaultMessageFilter(subData);

        assertTrue(filter.isMatchedByCommitLog(null, null));
        assertTrue(filter.isMatchedByCommitLog(ByteBuffer.allocate(10), null));
    }
}