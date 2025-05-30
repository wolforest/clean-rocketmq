package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;

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
