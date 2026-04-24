package cn.coderule.wolfmq.store.domain.commitlog.log;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
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
        factory = new CommitLogFactory(storeConfig, checkpoint);
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
