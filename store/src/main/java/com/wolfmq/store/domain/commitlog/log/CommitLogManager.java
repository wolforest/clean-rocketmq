package com.wolfmq.store.domain.commitlog.log;

import cn.coderule.common.convention.service.Lifecycle;
import cn.coderule.common.util.lang.collection.CollectionUtil;
import com.wolfmq.domain.config.store.CommitConfig;
import com.wolfmq.domain.domain.commitlog.CommitLog;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogManager implements Lifecycle {
    private final CommitConfig config;
    private final CommitLog[] commitLogArray;

    public CommitLogManager(CommitConfig config) {
        this.config = config;
        this.commitLogArray = new CommitLog[config.getShardingNumber()];
    }

    public CommitLog selectByRandom() {
        int index;
        if (1 == commitLogArray.length) {
            index = 0;
        } else {
            index = ThreadLocalRandom.current().nextInt(commitLogArray.length);
        }

        return selectByIndex(index);
    }

    public CommitLog selectFirst() {
        return selectByIndex(0);
    }

    public CommitLog selectByIndex(int index) {
        if (index < 0 || index >= commitLogArray.length) {
            throw new IllegalArgumentException("index out of range: " + index);
        }

        CommitLog commitLog = commitLogArray[index];
        if (commitLog == null) {
            throw new IllegalArgumentException("shardId related commitLog is null: " + index);
        }

        return commitLog;
    }

    public void addCommitLog(CommitLog commitLog) {
        commitLogArray[commitLog.getShardId()] = commitLog;
    }

    public void addCommitLog(List<CommitLog> logList) {
        if (CollectionUtil.isEmpty(logList)) return;

        for (CommitLog commitLog : logList) {
            addCommitLog(commitLog);
        }
    }

    @Override
    public void initialize() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.initialize();
        }
    }

    @Override
    public void start() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (CommitLog commitLog : commitLogArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.shutdown();
        }
    }


}
