package cn.coderule.minimq.store.domain.commitlog;

import cn.coderule.minimq.domain.core.enums.store.EnqueueStatus;
import cn.coderule.minimq.domain.core.exception.EnqueueException;
import cn.coderule.minimq.domain.domain.message.MessageBO;
import cn.coderule.minimq.domain.domain.store.domain.commitlog.CommitLog;
import cn.coderule.minimq.domain.domain.store.domain.mq.EnqueueFuture;
import cn.coderule.minimq.domain.domain.store.infra.InsertResult;
import cn.coderule.minimq.domain.domain.store.infra.SelectedMappedBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * @renamed from CommitLogFacade to CommitLogManager
 */
@Slf4j
public class CommitLogManager {
    private final List<CommitLog> commitLogList;
    private final Map<String, CommitLog> topicMap;
    private final Map<Integer, CommitLog> shardMap;

    public CommitLogManager() {
        commitLogList = new ArrayList<>();
        topicMap = new HashMap<>();
        shardMap = new HashMap<>();
    }

    public void addCommitLog(CommitLog commitLog) {
        commitLogList.add(commitLog);
        bindShard(commitLog);
    }

    public void bindTopic(String topic, CommitLog commitLog) {
        topicMap.put(topic, commitLog);
        bindShard(commitLog);
    }

    public EnqueueFuture insert(MessageBO messageBO) {
        try {
            return selectByTopic(messageBO.getRealTopic())
                .insert(messageBO);
        } catch (EnqueueException e) {
            return EnqueueFuture.failure(e.getStatus());
        }
    }

    public MessageBO select(String topic, long offset, int size) {
        try {
            CommitLog commitLog = selectByTopic(topic);
            return commitLog.select(offset, size);
        } catch (EnqueueException e) {
            return MessageBO.notFound();
        }
    }

    public MessageBO select(String topic, long offset) {
        try {
            CommitLog commitLog = selectByTopic(topic);
            return commitLog.select(offset);
        } catch (EnqueueException e) {
            return MessageBO.notFound();
        }
    }

    public InsertResult insert(String path, long offset, byte[] data, int start, int size) {
        CommitLog commitLog = selectByPath(path);
        if (commitLog == null) {
            return InsertResult.failure();
        }
        return commitLog.insert(offset, data, start, size);
    }

    public SelectedMappedBuffer selectBuffer(String path, long offset) {
        CommitLog commitLog = selectByPath(path);
        if (commitLog == null) {
            return null;
        }
        return commitLog.selectBuffer(offset);
    }

    public SelectedMappedBuffer selectBuffer(String path, long offset, int size) {
        CommitLog commitLog = selectByPath(path);
        if (commitLog == null) {
            return null;
        }
        return commitLog.selectBuffer(offset, size);
    }

    private void bindShard(CommitLog commitLog) {
        shardMap.put(commitLog.getShardId(), commitLog);
    }

    private CommitLog selectByTopic(String topic) {
        CommitLog commitLog;

        commitLog = topicMap.get(topic);
        if (null != commitLog) {
            return commitLog;
        }

        if (commitLogList.isEmpty()) {
            throw new EnqueueException(EnqueueStatus.COMMITLOG_NOT_FOUND);
        }

        int maxIndex = commitLogList.size() - 1;
        int randomIndex = (int) (Math.random() * maxIndex);
        return commitLogList.get(randomIndex);
    }

    private CommitLog selectByPath(String path) {
        return shardMap.get(path);
    }

}
