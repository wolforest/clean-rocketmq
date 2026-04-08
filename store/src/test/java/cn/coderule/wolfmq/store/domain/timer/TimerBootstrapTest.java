package cn.coderule.wolfmq.store.domain.timer;

import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.config.store.StorePath;
import cn.coderule.wolfmq.domain.domain.store.api.TimerStore;
import cn.coderule.wolfmq.domain.test.ConfigMock;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.timer.service.TimerService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerBootstrapTest {

    @Test
    void initialize_ShouldRegisterTimerBeans(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.getTimerConfig().setTimerLogFileSize(1024);
        storeConfig.getTimerConfig().setTotalSlots(8);
        storeConfig.getTimerConfig().setWheelSlots(4);
        storeConfig.getTimerConfig().setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        MessageService messageService = mock(MessageService.class);

        StoreContext.register(storeConfig);
        StoreContext.register(messageService);

        TimerBootstrap bootstrap = new TimerBootstrap();
        bootstrap.initialize();

        TimerService timerService = StoreContext.getBean(TimerService.class);
        TimerStore timerStore = StoreContext.getAPI(TimerStore.class);

        assertNotNull(timerService);
        assertNotNull(timerStore);
    }
}

