package cn.coderule.minimq.domain.domain.transaction;

import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface TransactionChannel {
    void check(MessageBO messageBO);
}
