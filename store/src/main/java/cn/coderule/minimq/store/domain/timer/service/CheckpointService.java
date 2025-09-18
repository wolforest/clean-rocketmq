package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.common.convention.ability.Flushable;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.io.DirUtil;
import cn.coderule.minimq.domain.config.server.StoreConfig;
import cn.coderule.minimq.domain.domain.meta.DataVersion;
import cn.coderule.minimq.domain.domain.timer.state.TimerCheckpoint;
import cn.coderule.minimq.store.infra.file.DefaultMappedFile;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CheckpointService implements Flushable, Lifecycle {
    private final StoreConfig storeConfig;
    private final String path;

    private final File file;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedByteBuffer;

    @Getter
    private final TimerCheckpoint checkpoint;

    public CheckpointService(StoreConfig storeConfig, String path) throws IOException {
        this.storeConfig = storeConfig;
        this.path = path;

        file = new File(path);
        DirUtil.createIfNotExists(file.getParent());

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();
        mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, DefaultMappedFile.OS_PAGE_SIZE);

        checkpoint = new TimerCheckpoint();
    }

    public void store(TimerCheckpoint checkpoint) {
        this.checkpoint.setLastReadTimeMs(checkpoint.getLastReadTimeMs());
        this.checkpoint.setLastTimerLogFlushPos(checkpoint.getLastTimerLogFlushPos());
        this.checkpoint.setLastTimerQueueOffset(checkpoint.getLastTimerQueueOffset());
        this.checkpoint.setMasterTimerQueueOffset(checkpoint.getMasterTimerQueueOffset());
    }

    public void flush() {

    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public void initialize() throws Exception {
        if (!file.exists()) {
            return;
        }

        checkpoint.setLastReadTimeMs(mappedByteBuffer.getLong(0));
        checkpoint.setLastTimerLogFlushPos(mappedByteBuffer.getLong(8));
        checkpoint.setLastTimerQueueOffset(mappedByteBuffer.getLong(16));
        checkpoint.setMasterTimerQueueOffset(mappedByteBuffer.getLong(24));

        if (this.mappedByteBuffer.hasRemaining()) {
            DataVersion version = checkpoint.getDataVersion();
            version.setStateVersion(mappedByteBuffer.getLong(32));
            version.setTimestamp(mappedByteBuffer.getLong(40));
            version.setCounter(new AtomicLong(mappedByteBuffer.getLong(48)));
        }

        log.info("Load timer checkpoint: {}", checkpoint);
    }
}
