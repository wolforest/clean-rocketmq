

/**
 * $Id: EndTransactionResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.broker.rpc.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;

public class EndTransactionResponseHeader implements CommandHeader {

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
