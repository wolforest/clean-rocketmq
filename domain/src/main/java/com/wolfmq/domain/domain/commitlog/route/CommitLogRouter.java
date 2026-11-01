package com.wolfmq.domain.domain.commitlog.route;

import com.wolfmq.domain.domain.commitlog.CommitLog;
import com.wolfmq.domain.domain.message.MessageBO;

public interface CommitLogRouter {
    CommitLog route(MessageBO messageBO);
    CommitLog route(long offset);
    CommitLog route(int shardId);
}
