package cn.coderule.minimq.store.domain.timer.service;

import cn.coderule.common.convention.ability.Flushable;
import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.io.DirUtil;
import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.ByteUtil;
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

    private final File file;
    private final FileChannel fileChannel;
    private final MappedByteBuffer mappedBuffer;

    @Getter
    private final TimerCheckpoint checkpoint;

    public CheckpointService(StoreConfig storeConfig, String path) throws IOException {
        this.storeConfig = storeConfig;

        file = new File(path);
        DirUtil.createIfNotExists(file.getParent());

        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        fileChannel = randomAccessFile.getChannel();

        mappedBuffer = fileChannel.map(
            FileChannel.MapMode.READ_WRITE, 0, DefaultMappedFile.OS_PAGE_SIZE);

        checkpoint = new TimerCheckpoint();
    }

    public void store(TimerCheckpoint checkpoint) {
        this.checkpoint.setLastReadTimeMs(checkpoint.getLastReadTimeMs());
        this.checkpoint.setLastTimerLogFlushPos(checkpoint.getLastTimerLogFlushPos());
        this.checkpoint.setLastTimerQueueOffset(checkpoint.getLastTimerQueueOffset());
        this.checkpoint.setMasterTimerQueueOffset(checkpoint.getMasterTimerQueueOffset());
    }

    public void flush() {
        if (null == mappedBuffer) {
            return;
        }

        mappedBuffer.putLong(0, checkpoint.getLastReadTimeMs());
        mappedBuffer.putLong(8, checkpoint.getLastTimerLogFlushPos());
        mappedBuffer.putLong(16, checkpoint.getLastTimerQueueOffset());
        mappedBuffer.putLong(24, checkpoint.getMasterTimerQueueOffset());

        //update dataVersion
        DataVersion version = checkpoint.getDataVersion();
        mappedBuffer.putLong(32, version.getStateVersion());
        mappedBuffer.putLong(40, version.getTimestamp());
        mappedBuffer.putLong(48, version.getCounter().get());

        mappedBuffer.force();
    }

    @Override
    public void start() throws Exception {

    }

    @Override
    public void shutdown() throws Exception {
        if (null == mappedBuffer) {
            return;
        }

        flush();

        ByteUtil.cleanBuffer(mappedBuffer);
        FileUtil.closeChannel(fileChannel);
    }

    @Override
    public void initialize() throws Exception {
        if (!file.exists()) {
            return;
        }

        checkpoint.setLastReadTimeMs(mappedBuffer.getLong(0));
        checkpoint.setLastTimerLogFlushPos(mappedBuffer.getLong(8));
        checkpoint.setLastTimerQueueOffset(mappedBuffer.getLong(16));
        checkpoint.setMasterTimerQueueOffset(mappedBuffer.getLong(24));
        setDataVersion();

        log.info("Load timer checkpoint: {}", checkpoint);
    }

    private void setDataVersion() {
        if (!this.mappedBuffer.hasRemaining()) {
            return;
        }

        DataVersion version = checkpoint.getDataVersion();
        version.setStateVersion(mappedBuffer.getLong(32));
        version.setTimestamp(mappedBuffer.getLong(40));
        version.setCounter(new AtomicLong(mappedBuffer.getLong(48)));
    }
}
