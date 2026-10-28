

/**
 * $Id: RegisterOrderTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class RegisterOrderTopicRequestHeader implements CommandHeader {
    @CFNotNull
    private String topic;
    @CFNotNull
    private String orderTopicString;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
