
package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNullable;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class RegisterBrokerResponseHeader implements CommandHeader {
    @CFNullable
    private String haServerAddr;
    @CFNullable
    private String masterAddr;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
