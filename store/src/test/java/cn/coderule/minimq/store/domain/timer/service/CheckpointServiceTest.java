package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class CheckpointServiceTest {

    @Test
    void flushAndInitialize_ShouldPersistCheckpoint(@TempDir Path tempDir) throws Exception {
        String path = tempDir.resolve("timercheck").toString();
        CheckpointService service = new CheckpointService(path);

        TimerCheckpoint checkpoint = new TimerCheckpoint();
        checkpoint.setLastReadTimeMs(100);
        checkpoint.setLastTimerLogFlushPos(200);
        checkpoint.setLastTimerQueueOffset(300);
        checkpoint.setMasterTimerQueueOffset(400);
        service.update(checkpoint);
        service.flush();
        service.shutdown();

        CheckpointService reloaded = new CheckpointService(path);
        reloaded.initialize();

        TimerCheckpoint loaded = reloaded.getCheckpoint();
        assertEquals(100, loaded.getLastReadTimeMs());
        assertEquals(200, loaded.getLastTimerLogFlushPos());
        assertEquals(300, loaded.getLastTimerQueueOffset());
        assertEquals(400, loaded.getMasterTimerQueueOffset());

        reloaded.shutdown();
    }
}

