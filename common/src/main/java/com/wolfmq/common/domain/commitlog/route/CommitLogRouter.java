package com.wolfmq.common.domain.commitlog.route;

import com.wolfmq.common.domain.commitlog.CommitLog;
import com.wolfmq.common.domain.message.MessageBO;

public interface CommitLogRouter {
    CommitLog route(MessageBO messageBO);
    CommitLog route(long offset);
    CommitLog route(int shardId);
}
