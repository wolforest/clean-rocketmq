package cn.coderule.minimq.store.domain.meta;

import cn.coderule.minimq.domain.core.constant.MQConstants;
import cn.coderule.minimq.domain.domain.meta.subscription.SubscriptionGroup;
import cn.coderule.minimq.domain.domain.store.domain.meta.ConsumeOffsetService;
import cn.coderule.minimq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefaultSubscriptionServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void load_WhenFileMissing_ShouldRegisterSystemGroups() {
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        DefaultSubscriptionService service = new DefaultSubscriptionService(
            tempDir.resolve("subscription.json").toString(),
            offsetService
        );

        service.load();

        assertTrue(service.existsGroup(MQConstants.TOOLS_CONSUMER_GROUP));
        assertTrue(service.existsGroup(MQConstants.FILTERSRV_CONSUMER_GROUP));
        assertTrue(service.existsGroup(MQConstants.SELF_TEST_CONSUMER_GROUP));
        assertTrue(service.existsGroup(MQConstants.CID_SYS_RMQ_TRANS));
    }

    @Test
    void saveAndDeleteGroup_ShouldPersistAndCleanOffsetWhenRequested() {
        StoreContext.setStateMachineVersion(1024L);
        ConsumeOffsetService offsetService = mock(ConsumeOffsetService.class);
        String storePath = tempDir.resolve("subscription.json").toString();
        DefaultSubscriptionService service = new DefaultSubscriptionService(storePath, offsetService);

        SubscriptionGroup group = new SubscriptionGroup();
        group.setGroupName("groupX");
        service.saveGroup(group);

        assertNotNull(service.getGroup("groupX"));

        service.deleteGroup("groupX", true);
        assertFalse(service.existsGroup("groupX"));
        verify(offsetService).deleteByGroup("groupX");
    }
}

