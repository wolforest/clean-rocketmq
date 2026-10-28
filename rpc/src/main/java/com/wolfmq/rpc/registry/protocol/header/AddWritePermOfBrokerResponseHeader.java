package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;

public class AddWritePermOfBrokerResponseHeader implements CommandHeader {
    @CFNotNull
    private Integer addTopicCount;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public Integer getAddTopicCount() {
        return addTopicCount;
    }

    public void setAddTopicCount(Integer addTopicCount) {
        this.addTopicCount = addTopicCount;
    }
}
