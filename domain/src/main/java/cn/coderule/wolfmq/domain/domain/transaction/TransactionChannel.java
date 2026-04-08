package cn.coderule.wolfmq.domain.domain.transaction;

import cn.coderule.wolfmq.domain.domain.message.MessageBO;

public interface TransactionChannel {
    void check(MessageBO messageBO);
}
