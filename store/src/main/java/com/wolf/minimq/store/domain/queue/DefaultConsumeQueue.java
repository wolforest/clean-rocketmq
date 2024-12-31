package com.wolf.minimq.store.domain.queue;

import com.wolf.minimq.domain.service.store.domain.ConsumeQueue;
import com.wolf.minimq.domain.model.dto.MessageContainer;

public class DefaultConsumeQueue implements ConsumeQueue {
    @Override
    public void assignOffset(MessageContainer context) {

    }

    @Override
    public void increaseOffset(MessageContainer context) {

    }
}
