package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.store.domain.meta.SubscriptionService;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SystemGroupRegisterTest {

    @Test
    void register_ShouldPutAllExpectedSystemGroups() {
        SubscriptionService subscriptionService = mock(SubscriptionService.class);
        SystemGroupRegister register = new SystemGroupRegister(subscriptionService);

        register.register();

        ArgumentCaptor<SubscriptionGroup> captor = ArgumentCaptor.forClass(SubscriptionGroup.class);
        verify(subscriptionService, times(8)).putGroup(captor.capture());

        Set<String> actual = captor.getAllValues()
            .stream()
            .map(SubscriptionGroup::getGroupName)
            .collect(Collectors.toSet());

        Set<String> expected = Set.of(
            MQConstants.TOOLS_CONSUMER_GROUP,
            MQConstants.FILTERSRV_CONSUMER_GROUP,
            MQConstants.SELF_TEST_CONSUMER_GROUP,
            MQConstants.ONS_HTTP_PROXY_GROUP,
            MQConstants.CID_ONSAPI_PULL_GROUP,
            MQConstants.CID_ONSAPI_PERMISSION_GROUP,
            MQConstants.CID_ONSAPI_OWNER_GROUP,
            MQConstants.CID_SYS_RMQ_TRANS
        );

        assertEquals(expected, actual);
    }
}

