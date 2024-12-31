package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.dto.MessageContainer;

public interface ConsumeQueue {
    void assignOffset(MessageContainer context);
    void increaseOffset(MessageContainer context);
}
