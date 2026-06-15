package cn.coderule.wolfmq.store.api;

import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.wolfmq.domain.domain.meta.subscription.SubscriptionRequest;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SubscriptionStoreImplTest {

    private SubscriptionService subscriptionService;
    private SubscriptionStoreImpl store;

    @BeforeEach
    void setUp() {
        subscriptionService = mock(SubscriptionService.class);
        store = new SubscriptionStoreImpl(subscriptionService);
    }

    @Test
    void existsGroup_ShouldDelegateToService() {
        when(subscriptionService.existsGroup("g1")).thenReturn(true);

        assertTrue(store.existsGroup("g1"));
        verify(subscriptionService).existsGroup("g1");
    }

    @Test
    void getGroup_ShouldDelegateToService() {
        SubscriptionGroup group = SubscriptionGroup.builder().groupName("g1").build();
        when(subscriptionService.getGroup("g1")).thenReturn(group);

        SubscriptionGroup result = store.getGroup("g1");

        assertEquals("g1", result.getGroupName());
        verify(subscriptionService).getGroup("g1");
    }

    @Test
    void saveGroup_ShouldDelegateToService() {
        SubscriptionGroup group = SubscriptionGroup.builder().groupName("g1").build();
        SubscriptionRequest request = SubscriptionRequest.builder().group(group).build();

        store.saveGroup(request);

        verify(subscriptionService).saveGroup(group);
    }

    @Test
    void deleteGroup_ShouldDelegateToService() {
        SubscriptionRequest request = SubscriptionRequest.builder()
            .groupName("g1")
            .cleanOffset(true)
            .build();

        store.deleteGroup(request);

        verify(subscriptionService).deleteGroup("g1", true);
    }

    @Test
    void deleteGroup_WithoutCleanOffset_ShouldDelegateToService() {
        SubscriptionRequest request = SubscriptionRequest.builder()
            .groupName("g1")
            .cleanOffset(false)
            .build();

        store.deleteGroup(request);

        verify(subscriptionService).deleteGroup("g1", false);
    }
}