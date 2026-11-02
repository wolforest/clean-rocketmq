package com.wolfmq.common.domain.transaction;

import com.wolfmq.common.domain.message.MessageBO;

public interface TransactionChannel {
    void check(MessageBO messageBO);
}
