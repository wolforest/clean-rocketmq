package com.wolfmq.store.domain.commitlog.file;

import cn.coderule.common.convention.service.Lifecycle;
import com.wolfmq.common.config.store.CommitConfig;
import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.message.MessageBO;
import com.wolfmq.common.domain.offset.commitlog.OffsetCodec;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommitLogManager implements Lifecycle {
    private final CommitConfig commitConfig;
    private final CommitLog[] logArray;
    private final CommitLogFactory logFactory;

    @Getter
    private final OffsetCodec offsetCodec;

    public CommitLogManager(CommitConfig config, CommitLogFactory logFactory) {
        this.commitConfig = config;
        this.logFactory = logFactory;

        this.offsetCodec = new OffsetCodec(config.getMaxShardingNumber());

        this.logArray = new CommitLog[config.getShardingNumber()];
        initLogArray();
    }

    public CommitLog selectByMessage(MessageBO message) {
        return selectByRandom();
    }

    public CommitLog selectByRandom() {
        int index;
        if (1 == logArray.length) {
            index = 0;
        } else {
            index = ThreadLocalRandom.current().nextInt(logArray.length);
        }

        return selectByShard(index);
    }

    public CommitLog selectFirst() {
        return selectByShard(0);
    }

    public CommitLog selectByOffset(long offset) {
        int shardId = offsetCodec.shardIdOf(offset);
        return selectByShard(shardId);
    }

    public CommitLog selectByShard(int index) {
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

    private void initLogArray() {
        for (int i = 0; i < commitConfig.getShardingNumber(); i++) {
            addCommitLog(
                i,
                logFactory.createByShardId(i)
            );
        }
    }

}
