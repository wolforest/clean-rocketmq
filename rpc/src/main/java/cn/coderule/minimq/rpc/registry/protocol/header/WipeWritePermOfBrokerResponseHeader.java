package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class WipeWritePermOfBrokerResponseHeader implements CommandHeader {
    @CFNotNull
    private Integer wipeTopicCount;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
