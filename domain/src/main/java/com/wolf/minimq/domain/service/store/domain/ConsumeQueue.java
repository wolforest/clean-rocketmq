package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.vo.MessageContext;

public interface ConsumeQueue {
    void assignOffset(MessageContext context);
    void increaseOffset(MessageContext context);
}
