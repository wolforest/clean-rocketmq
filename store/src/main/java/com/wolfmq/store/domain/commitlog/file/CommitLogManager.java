package com.wolfmq.store.domain.commitlog.file;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogManager implements Lifecycle {
    private final CommitConfig config;
    private final CommitLog[] logArray;
    private final OffsetCodec offsetCodec;

    public CommitLogManager(CommitConfig config) {
        this.config = config;
        this.logArray = new CommitLog[config.getShardingNumber()];
        this.offsetCodec = new OffsetCodec(config.getMaxShardingNumber());
    }

    public CommitLog selectByRandom() {
        int index;
        if (1 == logArray.length) {
            index = 0;
        } else {
            index = ThreadLocalRandom.current().nextInt(logArray.length);
        }

        return selectByIndex(index);
    }

    public CommitLog selectFirst() {
        return selectByIndex(0);
    }

    public CommitLog selectByIndex(int index) {
        if (index < 0 || index >= logArray.length) {
            throw new IllegalArgumentException("index out of range: " + index);
        }

        CommitLog commitLog = logArray[index];
        if (commitLog == null) {
            throw new IllegalArgumentException("shardId related commitLog is null: " + index);
        }

        return commitLog;
    }

    public void addCommitLog(int index, CommitLog commitLog) {
        logArray[index] = commitLog;
    }

    @Deprecated
    public void addCommitLog(CommitLog commitLog) {
        int index = offsetCodec.getShardId(
            commitLog.getMappedFileQueue().getMinOffset()
        );
        addCommitLog(index, commitLog);
    }

    @Override
    public void initialize() throws Exception {
        for (CommitLog commitLog : logArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.initialize();
        }
    }

    @Override
    public void start() throws Exception {
        for (CommitLog commitLog : logArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.start();
        }
    }

    @Override
    public void shutdown() throws Exception {
        for (CommitLog commitLog : logArray) {
            if (commitLog == null) {
                continue;
            }

            commitLog.shutdown();
        }
    }


}
