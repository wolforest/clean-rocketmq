package com.wolf.minimq.domain.service.store.domain;

import com.wolf.minimq.domain.model.bo.MessageBO;

public interface ConsumeQueue {
    void assignOffset(MessageBO messageBO);
    void increaseOffset(MessageBO messageBO);
}
