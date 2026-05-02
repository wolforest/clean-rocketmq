package cn.coderule.wolfmq.broker.server.grpc.service.consume;

import cn.coderule.wolfmq.domain.domain.cluster.RequestContext;
import cn.coderule.wolfmq.domain.domain.cluster.server.heartbeat.SubscriptionData;
import cn.coderule.wolfmq.domain.domain.consumer.hook.PopFilter.FilterResult;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPopFilterTest {

    private DefaultPopFilter popFilter;
    private RequestContext context;
    private SubscriptionData subscriptionData;

    @BeforeEach
    void setUp() {
        popFilter = new DefaultPopFilter(16);
        context = RequestContext.create("testGroup");
        subscriptionData = new SubscriptionData();
    }

    @Test
    void testFilterMessageMatch() {
        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        subscriptionData.setTagsSet(tags);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(0)
            .build();
        message.setTags("tag1");

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.MATCH, result);
    }

    @Test
    void testFilterMessageNoMatch() {
        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        subscriptionData.setTagsSet(tags);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(0)
            .build();
        message.setTags("tag2");

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.NO_MATCH, result);
    }

    @Test
    void testFilterMessageEmptyTagsSet() {
        // Empty tags set should match any tag
        subscriptionData.setTagsSet(new HashSet<>());

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(0)
            .build();
        message.setTags("anyTag");

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.MATCH, result);
    }

    @Test
    void testFilterMessageNullTags() {
        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        subscriptionData.setTagsSet(tags);

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(0)
            .build();
        message.setTags(null);

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.NO_MATCH, result);
    }

    @Test
    void testFilterMessageExceedMaxAttempts() {
        subscriptionData.setTagsSet(new HashSet<>());

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(16) // Equal to maxAttempts
            .build();

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.TO_DLQ, result);
    }

    @Test
    void testFilterMessageBelowMaxAttempts() {
        subscriptionData.setTagsSet(new HashSet<>());

        MessageBO message = MessageBO.builder()
            .topic("TestTopic")
            .reconsumeTimes(15) // Below maxAttempts
            .build();

        FilterResult result = popFilter.filterMessage(context, "testGroup", subscriptionData, message);

        assertEquals(FilterResult.MATCH, result);
    }

    @Test
    void testConstructor() {
        DefaultPopFilter filter = new DefaultPopFilter(10);
        assertNotNull(filter);
    }
}
