package cn.coderule.minimq.store.server.ha.client;

import cn.coderule.minimq.domain.service.store.api.CommitLogStore;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;

import static cn.coderule.minimq.store.server.ha.client.DefaultHAClient.REPORT_HEADER_SIZE;

@Slf4j
public class SlaveOffsetReporter {
    private final DefaultHAClient haClient;
    private final CommitLogStore commitLogStore;

    private long lastWriteTime;
    private long reportedOffset = 0;
    private final ByteBuffer reportBuffer = ByteBuffer.allocate(REPORT_HEADER_SIZE);

    public SlaveOffsetReporter(DefaultHAClient haClient, CommitLogStore commitLogStore) {
        this.haClient = haClient;
        this.commitLogStore = commitLogStore;
    }

    public boolean report(long offset) {
        return false;
    }
}
