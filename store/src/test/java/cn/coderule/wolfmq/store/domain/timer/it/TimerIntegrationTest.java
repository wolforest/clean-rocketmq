package cn.coderule.wolfmq.store.domain.timer.it;

import cn.coderule.wolfmq.domain.config.business.TimerConfig;
import cn.coderule.wolfmq.domain.config.server.StoreConfig;
import cn.coderule.wolfmq.domain.core.constant.MessageConst;
import cn.coderule.wolfmq.domain.domain.timer.ScanResult;
import cn.coderule.wolfmq.domain.domain.timer.TimerConstants;
import cn.coderule.wolfmq.domain.domain.timer.TimerEvent;
import cn.coderule.wolfmq.domain.domain.timer.wheel.Block;
import cn.coderule.wolfmq.domain.domain.timer.wheel.Slot;
import cn.coderule.wolfmq.domain.mock.ConfigMock;
import cn.coderule.wolfmq.domain.mock.MessageMock;
import cn.coderule.wolfmq.store.domain.timer.service.CheckpointService;
import cn.coderule.wolfmq.store.domain.timer.wheel.DefaultTimer;
import cn.coderule.wolfmq.store.domain.timer.wheel.TaskAdder;
import cn.coderule.wolfmq.store.domain.timer.wheel.TaskScanner;
import cn.coderule.wolfmq.store.domain.timer.wheel.TimerLog;
import cn.coderule.wolfmq.store.domain.timer.wheel.TimerWheel;
import cn.coderule.wolfmq.store.domain.mq.queue.MessageService;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogFactory;
import cn.coderule.wolfmq.store.domain.commitlog.log.CommitLogManager;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.TopicPartitioner;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueManager;
import cn.coderule.wolfmq.store.domain.consumequeue.queue.ConsumeQueueFactory;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueFlusher;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueLoader;
import cn.coderule.wolfmq.store.domain.consumequeue.service.ConsumeQueueRecovery;
import cn.coderule.wolfmq.store.domain.commitlog.sharding.OffsetCodec;
import cn.coderule.wolfmq.store.infra.file.AllocateMappedFileService;
import cn.coderule.wolfmq.store.server.bootstrap.StoreCheckpoint;
import cn.coderule.wolfmq.store.server.bootstrap.StoreContext;
import cn.coderule.wolfmq.domain.config.store.CommitConfig;
import cn.coderule.wolfmq.domain.config.store.ConsumeQueueConfig;
import cn.coderule.wolfmq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.wolfmq.domain.domain.store.domain.meta.TopicService;
import cn.coderule.wolfmq.domain.domain.store.server.CheckPoint;
import cn.coderule.wolfmq.domain.domain.message.MessageBO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimerIntegrationTest {

    private static final int FILE_SIZE = 1024 * 1024;
    private static final int TOTAL_SLOTS = 3600;
    private static final int PRECISION_MS = 1000;

    @TempDir
    Path tmpDir;

    private TimerLog timerLog;
    private TimerWheel timerWheel;
    private CheckpointService checkpointService;
    private StoreConfig storeConfig;
    private AllocateMappedFileService allocateService;
    private CommitLogManager commitLogManager;
    private MessageService messageService;

    @BeforeEach
    void setUp() throws Exception {
        resetStoreContext();

        String rootPath = tmpDir.toString();
        storeConfig = ConfigMock.createStoreConfig(rootPath);
        storeConfig.getCommitConfig().setFileSize(FILE_SIZE);
        storeConfig.getCommitConfig().setShardingNumber(1);
        storeConfig.getCommitConfig().setMaxShardingNumber(1);
        storeConfig.getTimerConfig().setTimerLogFileSize(FILE_SIZE);
        storeConfig.getTimerConfig().setTotalSlots(TOTAL_SLOTS);
        storeConfig.getTimerConfig().setPrecision(PRECISION_MS);

        StoreContext.register(storeConfig);
        StoreContext.register(storeConfig.getCommitConfig());

        StoreCheckpoint checkpoint = new StoreCheckpoint(tmpDir.resolve("checkpoint").toString());
        checkpoint.setShutdownSuccessful(false);
        StoreContext.setCheckPoint(checkpoint);

        allocateService = new AllocateMappedFileService(storeConfig);
        allocateService.start();
        StoreContext.register(allocateService);

        initCommitLog();
        initConsumeQueue();
        initTimer();
    }

    private void resetStoreContext() {
        StoreContext.CHECK_POINT = null;
        StoreContext.APPLICATION.getObjectMap().clear();
        StoreContext.API.getObjectMap().clear();
    }

    private void initCommitLog() throws Exception {
        CheckPoint checkpoint = StoreContext.getCheckPoint();
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        CommitLogFactory factory = new CommitLogFactory(storeConfig, checkpoint);
        List<CommitLog> logList = factory.createByConfig();

        TopicPartitioner partitioner = new TopicPartitioner(commitConfig);
        commitLogManager = new CommitLogManager(commitConfig, partitioner);
        commitLogManager.addCommitLog(logList);

        StoreContext.register(partitioner);
        StoreContext.register(commitLogManager);

        commitLogManager.initialize();
        commitLogManager.start();

        CommitLog cl = commitLogManager.selectByShardId(0);
        cl.getMappedFileQueue().getOrCreateMappedFileForSize(100);
    }

    private void initConsumeQueue() {
        ConsumeQueueConfig cqConfig = storeConfig.getConsumeQueueConfig();
        StoreCheckpoint checkpoint = StoreContext.getCheckPoint();
        CommitConfig commitConfig = storeConfig.getCommitConfig();

        TopicService topicService = mock(TopicService.class);
        when(topicService.exists(anyString())).thenReturn(true);
        StoreContext.register(topicService, TopicService.class);

        ConsumeQueueFlusher flusher = new ConsumeQueueFlusher(cqConfig, checkpoint);
        ConsumeQueueLoader loader = new ConsumeQueueLoader(cqConfig);
        OffsetCodec offsetCodec = new OffsetCodec(commitConfig.getMaxShardingNumber());
        ConsumeQueueRecovery recovery = new ConsumeQueueRecovery(cqConfig, checkpoint, offsetCodec);

        ConsumeQueueFactory factory = new ConsumeQueueFactory(cqConfig, topicService, checkpoint);
        factory.addCreateHook(flusher);
        factory.addCreateHook(loader);
        factory.addCreateHook(recovery);
        factory.createAll();

        ConsumeQueueManager consumeQueueManager = new ConsumeQueueManager(factory);
        StoreContext.register(consumeQueueManager, ConsumeQueueManager.class);

        messageService = new MessageService(commitLogManager, consumeQueueManager);
        StoreContext.register(messageService, MessageService.class);
    }

    private void initTimer() throws IOException {
        String timerLogPath = tmpDir.resolve("timerlog").toString();
        String timerWheelPath = tmpDir.resolve("timerwheel").toString();

        timerLog = new TimerLog(timerLogPath, FILE_SIZE);
        timerLog.load();

        timerWheel = new TimerWheel(timerWheelPath, TOTAL_SLOTS, PRECISION_MS);

        String checkpointPath = tmpDir.resolve("timercheck").toString();
        checkpointService = new CheckpointService(checkpointPath);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (timerWheel != null) {
            timerWheel.destroy();
        }
        if (timerLog != null) {
            timerLog.destroy();
        }
        if (checkpointService != null) {
            checkpointService.shutdown();
        }
        if (commitLogManager != null) {
            commitLogManager.shutdown();
        }
        if (allocateService != null) {
            allocateService.shutdown();
        }
        resetStoreContext();
    }

    @Test
    void testTimerLogAppendAndRead() {
        Block block = Block.builder()
            .size(Block.SIZE)
            .prevPos(-1)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .currWriteTime(System.currentTimeMillis())
            .delayedTime(5000)
            .offsetPy(0)
            .sizePy(128)
            .hashCodeOfRealTopic("TestTopic".hashCode())
            .reservedValue(0)
            .build();

        long offset = timerLog.append(block, 0, Block.SIZE);
        assertTrue(offset >= 0, "timerLog append should return non-negative offset");

        cn.coderule.wolfmq.domain.domain.store.infra.SelectedMappedBuffer buffer = timerLog.getTimerMessage(offset);
        assertNotNull(buffer, "should be able to read timer message at appended offset");
        buffer.release();
    }

    @Test
    void testTimerLogMultipleAppends() {
        long timestamp = System.currentTimeMillis();

        Block block1 = Block.builder()
            .size(Block.SIZE)
            .prevPos(-1)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .currWriteTime(timestamp)
            .delayedTime(3000)
            .offsetPy(100)
            .sizePy(50)
            .hashCodeOfRealTopic(0)
            .reservedValue(0)
            .build();

        long offset1 = timerLog.append(block1, 0, Block.SIZE);
        assertTrue(offset1 >= 0);

        Block block2 = Block.builder()
            .size(Block.SIZE)
            .prevPos(offset1)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .currWriteTime(timestamp)
            .delayedTime(3000)
            .offsetPy(200)
            .sizePy(60)
            .hashCodeOfRealTopic(0)
            .reservedValue(0)
            .build();

        long offset2 = timerLog.append(block2, 0, Block.SIZE);
        assertTrue(offset2 >= 0);
        assertTrue(offset2 > offset1, "second append offset should be greater than first");
    }

    @Test
    void testTimerWheelPutAndGetSlot() {
        long delayTime = System.currentTimeMillis() + 5000;
        long normalizedTime = delayTime / PRECISION_MS * PRECISION_MS;

        timerWheel.putSlot(normalizedTime, 100L, 200L);

        Slot slot = timerWheel.getSlot(normalizedTime);
        assertNotNull(slot);
        assertEquals(100L, slot.getFirstPos());
        assertEquals(200L, slot.getLastPos());
    }

    @Test
    void testTimerWheelGetEmptySlot() {
        long emptyTime = System.currentTimeMillis() + 999999;
        long normalizedTime = emptyTime / PRECISION_MS * PRECISION_MS;

        Slot slot = timerWheel.getSlot(normalizedTime);
        assertNotNull(slot);
        assertEquals(-1, slot.getTimeMs());
    }

    @Test
    void testTimerWheelSlotUpdate() {
        long delayTime = System.currentTimeMillis() + 5000;
        long normalizedTime = delayTime / PRECISION_MS * PRECISION_MS;

        timerWheel.putSlot(normalizedTime, 100L, 200L, 1, 0);

        Slot slot = timerWheel.getSlot(normalizedTime);
        assertNotNull(slot);
        assertEquals(1, slot.getNum());

        timerWheel.putSlot(normalizedTime, 300L, 400L, 5, 0);

        Slot updatedSlot = timerWheel.getSlot(normalizedTime);
        assertNotNull(updatedSlot);
        assertEquals(300L, updatedSlot.getFirstPos());
        assertEquals(400L, updatedSlot.getLastPos());
        assertEquals(5, updatedSlot.getNum());
    }

    @Test
    void testTimerWheelGetSlotIndex() {
        long time1 = 1000L;
        long time2 = 2000L;

        int index1 = timerWheel.getSlotIndex(time1);
        int index2 = timerWheel.getSlotIndex(time2);

        assertTrue(index1 < TOTAL_SLOTS * 2);
        assertTrue(index2 < TOTAL_SLOTS * 2);
        assertNotEquals(index1, index2, "different times should map to different slot indices");
    }

    @Test
    void testCheckpointServiceLifecycle() {
        cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint cp = checkpointService.getCheckpoint();
        assertNotNull(cp);
        assertEquals(0L, cp.getLastReadTimeMs());
        assertEquals(0L, cp.getLastTimerLogFlushPos());
    }

    @Test
    void testCheckpointServiceUpdate() {
        long testTime = System.currentTimeMillis();
        long testPos = 12345L;

        cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint update = new cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint();
        update.setLastReadTimeMs(testTime);
        update.setLastTimerLogFlushPos(testPos);

        checkpointService.update(update);

        cn.coderule.wolfmq.domain.domain.timer.state.TimerCheckpoint loaded = checkpointService.getCheckpoint();
        assertEquals(testTime, loaded.getLastReadTimeMs());
        assertEquals(testPos, loaded.getLastTimerLogFlushPos());
    }

    @Test
    void testDefaultTimerAddTimer() throws Exception {
        DefaultTimer timer = new DefaultTimer(storeConfig, checkpointService, messageService);
        timer.initialize();

        MessageBO messageBO = MessageMock.createMessage("TIMER_TEST", 100);
        messageBO.setCommitOffset(0);
        messageBO.setMessageLength(100);

        long delayTime = System.currentTimeMillis() + 5000;
        TimerEvent event = TimerEvent.builder()
            .commitLogOffset(0)
            .messageSize(100)
            .delayTime(delayTime)
            .enqueueTime(System.currentTimeMillis())
            .batchTime(System.currentTimeMillis())
            .messageBO(messageBO)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .build();

        boolean result = timer.addTimer(event);
        assertTrue(result, "addTimer should succeed");

        timer.shutdown();
    }

    @Test
    void testDefaultTimerScanEmpty() throws Exception {
        DefaultTimer timer = new DefaultTimer(storeConfig, checkpointService, messageService);
        timer.initialize();

        long scanTime = System.currentTimeMillis();
        ScanResult result = timer.scan(scanTime);

        assertNotNull(result);
        assertTrue(result.isEmpty() || result.getNormalMsgStack().isEmpty(),
            "scan on empty timer should return empty or no normal messages");

        timer.shutdown();
    }

    @Test
    void testDefaultTimerAddAndScan() throws Exception {
        DefaultTimer timer = new DefaultTimer(storeConfig, checkpointService, messageService);
        timer.initialize();

        long delayTime = System.currentTimeMillis() + 3000;
        MessageBO messageBO = MessageMock.createMessage("TIMER_SCAN_TEST", 100);
        messageBO.setCommitOffset(0);
        messageBO.setMessageLength(100);

        TimerEvent event = TimerEvent.builder()
            .commitLogOffset(0)
            .messageSize(100)
            .delayTime(delayTime)
            .enqueueTime(System.currentTimeMillis())
            .batchTime(delayTime - 3000)
            .messageBO(messageBO)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .build();

        boolean added = timer.addTimer(event);
        assertTrue(added, "addTimer should succeed");

        ScanResult result = timer.scan(delayTime);
        assertNotNull(result, "scan result should not be null");

        timer.shutdown();
    }

    @Test
    void testTimerLogFlush() {
        Block block = Block.builder()
            .size(Block.SIZE)
            .prevPos(-1)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .currWriteTime(System.currentTimeMillis())
            .delayedTime(5000)
            .offsetPy(0)
            .sizePy(100)
            .hashCodeOfRealTopic(0)
            .reservedValue(0)
            .build();

        long offset = timerLog.append(block, 0, Block.SIZE);
        assertTrue(offset >= 0);

        long flushPos = timerLog.getFlushPosition();
        assertTrue(flushPos >= 0, "flush position should be non-negative");
    }

    @Test
    void testBlockCreationAndSerialization() {
        long timestamp = System.currentTimeMillis();

        Block block = Block.builder()
            .size(Block.SIZE)
            .prevPos(42L)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .currWriteTime(timestamp)
            .delayedTime(3000)
            .offsetPy(100L)
            .sizePy(256)
            .hashCodeOfRealTopic("TopicA".hashCode())
            .reservedValue(0L)
            .build();

        byte[] bytes = block.bytes();
        assertNotNull(bytes);
        assertEquals(Block.SIZE, bytes.length, "Block bytes length should equal Block.SIZE");
        assertEquals(Block.SIZE, block.getSize());
        assertEquals(42L, block.getPrevPos());
        assertEquals(TimerConstants.MAGIC_DEFAULT, block.getMagic());
        assertEquals(100L, block.getOffsetPy());
        assertEquals(256, block.getSizePy());
    }

    @Test
    void testTaskAdderAndScannerEndToEnd() throws Exception {
        String timerLogPath = tmpDir.resolve("timerlog-e2e").toString();
        String timerWheelPath = tmpDir.resolve("timerwheel-e2e").toString();

        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setTimerLogFileSize(FILE_SIZE);
        timerConfig.setTotalSlots(TOTAL_SLOTS);
        timerConfig.setPrecision(PRECISION_MS);
        timerConfig.setWheelSlots(TOTAL_SLOTS);

        TimerLog e2eTimerLog = new TimerLog(timerLogPath, FILE_SIZE);
        e2eTimerLog.load();
        TimerWheel e2eTimerWheel = new TimerWheel(timerWheelPath, TOTAL_SLOTS, PRECISION_MS);

        TaskAdder adder = new TaskAdder(storeConfig, e2eTimerLog, e2eTimerWheel);
        TaskScanner scanner = new TaskScanner(storeConfig, e2eTimerLog, e2eTimerWheel);

        long batchTime = System.currentTimeMillis();
        long delayTime = batchTime + 3000;

        MessageBO message = MessageMock.createMessage("E2E_TIMER_TOPIC", 128, 0, 0);
        message.putProperty(cn.coderule.wolfmq.domain.core.constant.MessageConst.PROPERTY_REAL_TOPIC, "E2E_TIMER_TOPIC");

        TimerEvent event = TimerEvent.builder()
            .batchTime(batchTime)
            .delayTime(delayTime)
            .commitLogOffset(500L)
            .messageSize(128)
            .messageBO(message)
            .magic(TimerConstants.MAGIC_DEFAULT)
            .build();

        boolean added = adder.addTimer(event);
        assertTrue(added, "TaskAdder should successfully add timer event");

        Slot slot = e2eTimerWheel.getSlot(delayTime);
        assertTrue(slot.getTimeMs() > 0, "TimerWheel should have a slot at delay time");
        assertEquals(1, slot.getNum(), "Slot should have 1 entry");

        ScanResult result = scanner.scan(delayTime);
        assertTrue(result.isSuccess(), "TaskScanner scan should succeed");
        assertEquals(1, result.sizeOfNormalMsgStack(), "Scan should find 1 normal message");

        TimerEvent scannedEvent = result.getNormalMsgStack().getFirst();
        assertEquals(500L, scannedEvent.getCommitLogOffset(), "Scanned event should have correct commit log offset");
        assertEquals(delayTime, scannedEvent.getDelayTime(), "Scanned event should have correct delay time");

        e2eTimerWheel.destroy();
        e2eTimerLog.destroy();
    }

    @Test
    void testTaskAdderMultipleEventsAndScan() throws Exception {
        String timerLogPath = tmpDir.resolve("timerlog-multi").toString();
        String timerWheelPath = tmpDir.resolve("timerwheel-multi").toString();

        TimerConfig timerConfig = storeConfig.getTimerConfig();
        timerConfig.setTimerLogFileSize(FILE_SIZE);
        timerConfig.setTotalSlots(TOTAL_SLOTS);
        timerConfig.setPrecision(PRECISION_MS);
        timerConfig.setWheelSlots(TOTAL_SLOTS);

        TimerLog multiTimerLog = new TimerLog(timerLogPath, FILE_SIZE);
        multiTimerLog.load();
        TimerWheel multiTimerWheel = new TimerWheel(timerWheelPath, TOTAL_SLOTS, PRECISION_MS);

        TaskAdder adder = new TaskAdder(storeConfig, multiTimerLog, multiTimerWheel);
        TaskScanner scanner = new TaskScanner(storeConfig, multiTimerLog, multiTimerWheel);

        long batchTime = System.currentTimeMillis();
        long delayTime1 = batchTime + 2000;
        long delayTime2 = batchTime + 3000;

        MessageBO msg1 = MessageMock.createMessage("MULTI_TOPIC_1", 128, 0, 0);
        msg1.putProperty(cn.coderule.wolfmq.domain.core.constant.MessageConst.PROPERTY_REAL_TOPIC, "MULTI_TOPIC_1");

        TimerEvent event1 = TimerEvent.builder()
            .batchTime(batchTime).delayTime(delayTime1).commitLogOffset(100L)
            .messageSize(128).messageBO(msg1).magic(TimerConstants.MAGIC_DEFAULT).build();
        adder.addTimer(event1);

        MessageBO msg2 = MessageMock.createMessage("MULTI_TOPIC_2", 128, 0, 0);
        msg2.putProperty(cn.coderule.wolfmq.domain.core.constant.MessageConst.PROPERTY_REAL_TOPIC, "MULTI_TOPIC_2");

        TimerEvent event2 = TimerEvent.builder()
            .batchTime(batchTime).delayTime(delayTime1).commitLogOffset(200L)
            .messageSize(128).messageBO(msg2).magic(TimerConstants.MAGIC_DEFAULT).build();
        adder.addTimer(event2);

        Slot slot = multiTimerWheel.getSlot(delayTime1);
        assertEquals(2, slot.getNum(), "Slot should have 2 entries for same delay time");

        ScanResult result = scanner.scan(delayTime1);
        assertEquals(2, result.sizeOfNormalMsgStack(), "Scan should find 2 normal messages at same delay time");

        ScanResult emptyResult = scanner.scan(delayTime2);
        assertTrue(emptyResult.isEmpty() || emptyResult.sizeOfNormalMsgStack() == 0,
            "Scan at different delay time should find 0 messages");

        multiTimerWheel.destroy();
        multiTimerLog.destroy();
    }
}
