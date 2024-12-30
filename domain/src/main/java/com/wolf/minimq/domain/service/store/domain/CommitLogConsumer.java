package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.CommitLogEvent;

public interface CommitLogConsumer {
    void consume(CommitLogEvent event);
}
