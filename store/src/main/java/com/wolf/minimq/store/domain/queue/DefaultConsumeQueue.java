package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.model.dto.MessageContext;

public class DefaultConsumeQueue implements ConsumeQueue {
    @Override
    public void assignOffset(MessageContext context) {

    }

    @Override
    public void increaseOffset(MessageContext context) {

    }
}
