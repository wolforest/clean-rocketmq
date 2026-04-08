package cn.coderule.wolfmq.rpc.common.rpc.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;

public interface CommandHeader {
    void checkFields() throws RemotingCommandException;
}
