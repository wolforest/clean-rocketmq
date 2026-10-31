package com.wolfmq.store.domain.timer;

import com.wolfmq.domain.core.constant.ContextNamespace;

import cn.coderule.common.convention.container.ApplicationContext;

import com.wolfmq.domain.config.server.StoreConfig;
import com.wolfmq.domain.config.store.StorePath;
import com.wolfmq.domain.server.store.api.TimerStore;
import com.wolfmq.domain.test.ConfigMock;
import com.wolfmq.store.domain.mq.queue.MessageService;
import com.wolfmq.store.domain.timer.service.TimerService;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerBootstrapTest {
    protected ApplicationContext context = new ApplicationContext();


    @Test
    void initialize_ShouldRegisterTimerBeans(@TempDir Path tempDir) throws Exception {
        StoreConfig storeConfig = ConfigMock.createStoreConfig(tempDir.toString());
        storeConfig.getTimer().setTimerLogFileSize(1024);
        storeConfig.getTimer().setTotalSlots(8);
        storeConfig.getTimer().setWheelSlots(4);
        storeConfig.getTimer().setPrecision(1000);

        StorePath.initPath(tempDir.toString());
        MessageService messageService = mock(MessageService.class);

        context.register(storeConfig);
        context.register(messageService);

        TimerBootstrap bootstrap = new TimerBootstrap(context);
        bootstrap.initialize();

        TimerService timerService = context.getBean(TimerService.class);
        TimerStore timerStore = context.getBean(TimerStore.class, ContextNamespace.API);

        assertNotNull(timerService);
        assertNotNull(timerStore);
    }
}

