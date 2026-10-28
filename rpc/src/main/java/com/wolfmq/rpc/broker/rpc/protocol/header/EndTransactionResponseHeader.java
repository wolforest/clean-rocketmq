

/**
 * $Id: EndTransactionResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package com.wolfmq.rpc.broker.rpc.protocol.header;

import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;

public class EndTransactionResponseHeader implements CommandHeader {

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
