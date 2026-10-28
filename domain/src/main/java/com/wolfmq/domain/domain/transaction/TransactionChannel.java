package com.wolfmq.domain.domain.transaction;

import com.wolfmq.domain.domain.message.MessageBO;

public interface TransactionChannel {
    void check(MessageBO messageBO);
}
