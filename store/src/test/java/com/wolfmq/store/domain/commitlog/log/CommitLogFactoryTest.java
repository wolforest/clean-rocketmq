package com.wolfmq.store.domain.commitlog.log;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.server.store.CheckPoint;
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
        storeConfig.setCommitConfig(commitConfig);
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
