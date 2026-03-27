package cn.coderule.minimq.store.domain.commitlog.log;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import cn.coderule.minimq.domain.config.store.CommitConfig;
import cn.coderule.minimq.domain.core.exception.EnqueueException;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import cn.coderule.minimq.store.domain.commitlog.sharding.TopicPartitioner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitLogFacade to CommitLogManager
 */
@Slf4j
public class CommitLogManager implements Lifecycle {
    private final CommitConfig config;
    private final TopicPartitioner partitioner;

    private final List<CommitLog> commitLogList;
    private final Map<Integer, CommitLog> shardMap;

    public CommitLogManager(CommitConfig config, TopicPartitioner partitioner) {
        this.config = config;
        this.partitioner = partitioner;

        commitLogList = new ArrayList<>();
        shardMap = new HashMap<>();
    }

    public void addCommitLog(List<CommitLog> logList) {
        if (CollectionUtil.isEmpty(logList)) return;

        for (CommitLog commitLog : logList) {
            addCommitLog(commitLog);
        }
    }

    public void addCommitLog(CommitLog commitLog) {
        commitLogList.add(commitLog);
        bindShard(commitLog);
    }

    public EnqueueFuture insert(MessageBO messageBO) {
        try {
            int shardId = partitioner.partitionByTopic(messageBO.getTopic());

            return selectByShardId(shardId)
                .insert(messageBO);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]insert message failed: {}",  e.getMessage());
            return EnqueueFuture.failure(e.getStatus());
        }
    }

    public MessageBO select(long offset, int size) {
        try {
            int shardId = offsetToShardId(offset);
            CommitLog commitLog = selectByShardId(shardId);
            return commitLog.select(offset, size);
        } catch (EnqueueException e) {
            log.error("[CommitLogManager]select message failed: {}",  e.getMessage());
            return MessageBO.notFound();
        }
    }

    public MessageBO select(long offset) {
        try {
            int shardId = offsetToShardId(offset);
            CommitLog commitLog = selectByShardId(shardId);
            return commitLog.select(offset);
        } catch (EnqueueException e) {
            return MessageBO.notFound();
        }
    }

    public InsertResult insert(long offset, byte[] data, int start, int size) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.insert(offset, data, start, size);
    }

    public SelectedMappedBuffer selectBuffer(long offset) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.selectBuffer(offset);
    }

    public SelectedMappedBuffer selectBuffer(long offset, int size) {
        int shardId = offsetToShardId(offset);
        CommitLog commitLog = selectByShardId(shardId);
        return commitLog.selectBuffer(offset, size);
    }

    private void bindShard(CommitLog commitLog) {
        shardMap.put(commitLog.getShardId(), commitLog);
    }

    public CommitLog selectByShardId(Integer shardId) {
        CommitLog commitLog = shardMap.get(shardId);
        if (commitLog == null) {
            log.error("[CommitLogManager]shardId of commitLog not found: {}", shardId);
            throw new IllegalArgumentException("shardId of commitLog not found: " + shardId);
        }

        return commitLog;
    }

    public long getMinOffset(int shardId) {
        return selectByShardId(shardId).getMinOffset();
    }

    public long getMaxOffset(int shardId) {
        return selectByShardId(shardId).getMaxOffset();
    }

    public long getFlushedOffset(int shardId) {
        return selectByShardId(shardId).getFlushedOffset();
    }

    public long getUnFlushedSize(int shardId) {
        return selectByShardId(shardId).getUnFlushedSize();
    }

    public int offsetToShardId(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }

        return (int) (offset % config.getMaxShardingNumber());
    }

    @Override
    public void start() throws Exception {
        for (CommitLog commitLog : commitLogList) {
            commitLog.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (CommitLog commitLog : commitLogList) {
            commitLog.shutdown();
        }
    }

    @Override
    public void initialize() throws Exception {
        for (CommitLog commitLog : commitLogList) {
            commitLog.initialize();
        }
    }
}
