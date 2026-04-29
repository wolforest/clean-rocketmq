package cn.coderule.wolfmq.broker.domain.meta;

import cn.coderule.wolfmq.broker.infra.store.SubscriptionStore;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionServiceTest {

    @Mock
    private SubscriptionStore subscriptionStore;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subscriptionService = new SubscriptionService(subscriptionStore);
    }

    @Test
    void testConstructor() {
        assertNotNull(subscriptionService);
    }

    @Test
    void testGetGroupAsync() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        SubscriptionGroup expectedGroup = new SubscriptionGroup();
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(CompletableFuture.completedFuture(expectedGroup));

        CompletableFuture<SubscriptionGroup> future = subscriptionService.getGroupAsync(topicName, groupName);

        assertNotNull(future);
        verify(subscriptionStore).getGroupAsync(topicName, groupName);
    }

    @Test
    void testGetGroupSuccess() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        SubscriptionGroup expectedGroup = new SubscriptionGroup();
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(CompletableFuture.completedFuture(expectedGroup));

        SubscriptionGroup result = subscriptionService.getGroup(topicName, groupName);

        assertEquals(expectedGroup, result);
    }

    @Test
    void testGetGroupException() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        
        CompletableFuture<SubscriptionGroup> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("test error"));
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(failedFuture);

        SubscriptionGroup result = subscriptionService.getGroup(topicName, groupName);

        assertNull(result);
    }

    @Test
    void testIsConsumeOrderlyTrue() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        
        SubscriptionGroup group = new SubscriptionGroup();
        group.setConsumeMessageOrderly(true);
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(CompletableFuture.completedFuture(group));

        boolean result = subscriptionService.isConsumeOrderly(topicName, groupName);

        assertTrue(result);
    }

    @Test
    void testIsConsumeOrderlyFalse() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        
        SubscriptionGroup group = new SubscriptionGroup();
        group.setConsumeMessageOrderly(false);
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(CompletableFuture.completedFuture(group));

        boolean result = subscriptionService.isConsumeOrderly(topicName, groupName);

        assertFalse(result);
    }

    @Test
    void testIsConsumeOrderlyNullGroup() {
        String topicName = "TestTopic";
        String groupName = "TestGroup";
        
        when(subscriptionStore.getGroupAsync(topicName, groupName))
            .thenReturn(CompletableFuture.completedFuture(null));

        boolean result = subscriptionService.isConsumeOrderly(topicName, groupName);

        assertFalse(result);
    }
}
