package com.wolfmq.rpc.common.rpc.protocol.header;

import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;

public interface CommandHeader {
    void checkFields() throws RemotingCommandException;
}
