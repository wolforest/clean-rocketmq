package cn.coderule.wolfmq.rpc.broker.core;

import cn.coderule.wolfmq.domain.core.enums.consume.ExpressionType;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterAPITest {

    @Test
    void buildSubscriptionDataWithSubAllReturnsSubAll() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "*");
        assertEquals("*", data.getSubString());
    }

    @Test
    void buildSubscriptionDataWithNullReturnsSubAll() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", null);
        assertEquals("*", data.getSubString());
    }

    @Test
    void buildSubscriptionDataWithEmptyReturnsSubAll() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "");
        assertEquals("*", data.getSubString());
    }

    @Test
    void buildSubscriptionDataWithSingleTag() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "tag1");
        assertEquals("testTopic", data.getTopic());
        assertTrue(data.getTagsSet().contains("tag1"));
        assertTrue(data.getCodeSet().contains("tag1".hashCode()));
    }

    @Test
    void buildSubscriptionDataWithMultipleTags() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "tag1||tag2");
        assertTrue(data.getTagsSet().contains("tag1"));
        assertTrue(data.getTagsSet().contains("tag2"));
        assertTrue(data.getCodeSet().contains("tag1".hashCode()));
        assertTrue(data.getCodeSet().contains("tag2".hashCode()));
    }

    @Test
    void buildSubscriptionDataSetsTopicCorrectly() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("myTopic", "*");
        assertEquals("myTopic", data.getTopic());
    }

    @Test
    void buildWithTagTypeDelegatesToBuildSubscriptionData() throws Exception {
        SubscriptionData data = FilterAPI.build("testTopic", "tag1", ExpressionType.TAG);
        assertTrue(data.getTagsSet().contains("tag1"));
    }

    @Test
    void buildWithNullTypeDelegatesToBuildSubscriptionData() throws Exception {
        SubscriptionData data = FilterAPI.build("testTopic", "*", null);
        assertEquals("*", data.getSubString());
    }

    @Test
    void buildWithSql92TypeCreatesSubscriptionData() throws Exception {
        SubscriptionData data = FilterAPI.build("testTopic", "a > 10", ExpressionType.SQL92);
        assertEquals("testTopic", data.getTopic());
        assertEquals("a > 10", data.getSubString());
        assertEquals(ExpressionType.SQL92, data.getExpressionType());
    }

    @Test
    void buildWithSql92TypeAndEmptySubStringThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            FilterAPI.build("testTopic", "", ExpressionType.SQL92));
    }

    @Test
    void buildWithSql92TypeAndNullSubStringThrows() {
        assertThrows(IllegalArgumentException.class, () ->
            FilterAPI.build("testTopic", null, ExpressionType.SQL92));
    }

    @Test
    void buildSubscriptionDataWithExpressionType() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "*", ExpressionType.SQL92);
        assertEquals(ExpressionType.SQL92, data.getExpressionType());
    }

    @Test
    void buildSubscriptionDataWithBlankExpressionType() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "*", "");
        assertEquals(ExpressionType.TAG, data.getExpressionType());
    }

    @Test
    void buildSubscriptionDataWithDuplicateTagsDedupedBySet() throws Exception {
        SubscriptionData data = FilterAPI.buildSubscriptionData("testTopic", "tag1||tag2||tag1");
        assertEquals(2, data.getTagsSet().size());
        assertTrue(data.getTagsSet().contains("tag1"));
        assertTrue(data.getTagsSet().contains("tag2"));
    }
}