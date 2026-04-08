package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class WipeWritePermOfBrokerResponseHeader implements CommandHeader {
    @CFNotNull
    private Integer wipeTopicCount;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
