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

@Slf4j
public class CommitLogDispatcher {
    private final List<CommitLog> commitLogList;
    private final Map<String, CommitLog> topicMap;
    private final Map<String, CommitLog> pathMap;

    public CommitLogDispatcher() {
        commitLogList = new ArrayList<>();
        topicMap = new HashMap<>();
        pathMap = new HashMap<>();
    }

    public void addCommitLog(CommitLog commitLog) {
        commitLogList.add(commitLog);
    }

    public void bindTopic(String topic, CommitLog commitLog) {
        topicMap.put(topic, commitLog);
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
        return null;
    }

    public MessageBO select(String topic, long offset) {
        return null;
    }

    public InsertResult insert(String path, long offset, byte[] data, int start, int size) {
        return null;
    }

    public SelectedMappedBuffer selectBuffer(String path, long offset) {
        return null;
    }

    public SelectedMappedBuffer selectBuffer(String path, long offset, int size) {
        return null;
    }

    private void bindPath(CommitLog commitLog) {
        String rootDir = commitLog.getMappedFileQueue().getRootDir();
        pathMap.put(rootDir, commitLog);
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
        return pathMap.get(path);
    }

}
