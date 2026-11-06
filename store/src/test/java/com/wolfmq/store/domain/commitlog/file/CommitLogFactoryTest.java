package com.wolfmq.store.domain.commitlog.file;

import com.wolfmq.common.config.server.StoreConfig;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.offset.checkpoint.bakeup.CheckPoint;
import com.wolfmq.store.infra.file.AllocateMappedFileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CommitLogFactoryTest {

    private CommitLogFactory factory;

    @BeforeEach
    void setUp() {
        StoreConfig storeConfig = new StoreConfig();
        CommitConfig commitConfig = new CommitConfig();
        storeConfig.setCommit(commitConfig);
        CheckPoint checkpoint = mock(CheckPoint.class);
        factory = new CommitLogFactory(storeConfig, checkpoint, mock(AllocateMappedFileService.class));
    }

    @Test
    void testCreateByShardId() {
        assertDoesNotThrow(() -> factory.createByShardId(0));
    }

    @Test
    void testCreateByConfig() {
        assertDoesNotThrow(() -> factory.createByConfig());
    }
}
