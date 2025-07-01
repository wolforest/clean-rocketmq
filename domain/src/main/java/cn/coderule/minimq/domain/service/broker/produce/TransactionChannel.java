package cn.coderule.minimq.domain.service.broker.produce;

import cn.coderule.minimq.domain.domain.message.MessageBO;

public interface TransactionChannel {
    void check(MessageBO messageBO);
}
