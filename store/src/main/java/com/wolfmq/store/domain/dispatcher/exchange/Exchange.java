package com.wolfmq.store.domain.dispatcher.exchange;

import com.wolfmq.common.domain.message.MessageBO;

public interface Exchange {
    void exchange(MessageBO messageBO);
}
