package cn.coderule.minimq.rpc.common.rpc.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;

public interface CommandHeader {
    void checkFields() throws RemotingCommandException;
}
