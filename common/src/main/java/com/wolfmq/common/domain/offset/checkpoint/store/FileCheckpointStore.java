package com.wolfmq.common.domain.offset.checkpoint.store;

import cn.coderule.common.util.io.FileUtil;
import cn.coderule.common.util.lang.string.JSONUtil;
import cn.coderule.common.util.lang.string.StringUtil;
import com.wolfmq.common.domain.offset.checkpoint.CheckpointService;
import java.io.File;
import java.util.Map;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FileCheckpointStore implements CheckpointService, CheckpointStore {
    private static final String CHECKPOINT_FILE = "checkpoint.json";

    private final String storePath;
    private MemoryCheckpointStore memoryStore = new MemoryCheckpointStore();

    public FileCheckpointStore(String storePath) {
        this.storePath = storePath;
    }

    @Override
    public void setCommitOffset(int shard, long commitOffset) {
        memoryStore.setCommitOffset(shard, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shard) {
        return memoryStore.getCommitOffset(shard);
    }

    @Override
    public void setDispatchedOffset(int shard, long commitOffset) {
        memoryStore.setDispatchedOffset(shard, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shard) {
        return memoryStore.getDispatchedOffset(shard);
    }

    @Override
    public void setMasterOffset(int shard, long commitOffset) {
        memoryStore.setMasterOffset(shard, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shard) {
        return memoryStore.getMasterOffset(shard);
    }

    @Override
    public void setMasterOffset(Map<Integer, Long> offsetMap) {
        memoryStore.setMasterOffset(offsetMap);
    }

    @Override
    public void setIndexedOffset(int shard, long indexOffset) {
        memoryStore.setIndexedOffset(shard, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shard) {
        return memoryStore.getIndexedOffset(shard);
    }

    @Override
    public void setQueueOffset(String topic, int queueId, int shard, long queueOffset) {
        memoryStore.setQueueOffset(topic, queueId, shard, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId, int shard) {
        return memoryStore.getQueueOffset(topic, queueId, shard);
    }

    @Override
    public void setQueueCommitOffset(String topic, int queueId, int shard, long commitOffset) {
        memoryStore.setQueueCommitOffset(topic, queueId, shard, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shard) {
        return memoryStore.getQueueCommitOffset(topic, queueId, shard);
    }

    @Override
    public void load() {
        String path = getCheckpointFilePath();
        if (StringUtil.isBlank(path)) {
            return;
        }

        if (!FileUtil.exists(path)) {
            return;
        }

        String data = FileUtil.readString(path);
        if (StringUtil.isBlank(data)) {
            return;
        }

        MemoryCheckpointStore loaded = JSONUtil.parse(data, MemoryCheckpointStore.class);
        if (loaded != null) {
            memoryStore = loaded;
        }
    }

    @Override
    public void save() {
        String path = getCheckpointFilePath();
        if (StringUtil.isBlank(path)) {
            log.warn("checkpoint store path is empty, skip saving");
            return;
        }

        String data = JSONUtil.toJSONString(memoryStore);
        FileUtil.stringToFile(data, path);
    }

    private String getCheckpointFilePath() {
        if (StringUtil.isBlank(storePath)) {
            return null;
        }

        if (storePath.endsWith(".json")) {
            return storePath;
        }

        return storePath + File.separator + CHECKPOINT_FILE;
    }
}
