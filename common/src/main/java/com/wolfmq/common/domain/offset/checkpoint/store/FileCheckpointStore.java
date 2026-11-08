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
    public void updateCommitOffset(int shardId, long commitOffset) {
        memoryStore.updateCommitOffset(shardId, commitOffset);
    }

    @Override
    public Long getCommitOffset(int shardId) {
        return memoryStore.getCommitOffset(shardId);
    }

    @Override
    public void updateDispatchedOffset(int shardId, long commitOffset) {
        memoryStore.updateDispatchedOffset(shardId, commitOffset);
    }

    @Override
    public Long getDispatchedOffset(int shardId) {
        return memoryStore.getDispatchedOffset(shardId);
    }

    @Override
    public void updateMasterOffset(int shardId, long commitOffset) {
        memoryStore.updateMasterOffset(shardId, commitOffset);
    }

    @Override
    public Long getMasterOffset(int shardId) {
        return memoryStore.getMasterOffset(shardId);
    }

    @Override
    public void updateMasterOffsets(Map<Integer, Long> offsetMap) {
        memoryStore.updateMasterOffsets(offsetMap);
    }

    @Override
    public void updateIndexedOffset(int shardId, long indexOffset) {
        memoryStore.updateIndexedOffset(shardId, indexOffset);
    }

    @Override
    public Long getIndexedOffset(int shardId) {
        return memoryStore.getIndexedOffset(shardId);
    }

    @Override
    public void updateQueueOffset(String topic, int queueId, long queueOffset) {
        memoryStore.updateQueueOffset(topic, queueId, queueOffset);
    }

    @Override
    public Long getQueueOffset(String topic, int queueId) {
        return memoryStore.getQueueOffset(topic, queueId);
    }

    @Override
    public void updateQueueCommitOffset(String topic, int queueId, int shardId, long commitOffset) {
        memoryStore.updateQueueCommitOffset(topic, queueId, shardId, commitOffset);
    }

    @Override
    public Long getQueueCommitOffset(String topic, int queueId, int shardId) {
        return memoryStore.getQueueCommitOffset(topic, queueId, shardId);
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
