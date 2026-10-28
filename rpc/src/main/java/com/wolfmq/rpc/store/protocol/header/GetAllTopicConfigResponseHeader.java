

/**
 * $Id: GetAllTopicConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package com.wolfmq.rpc.store.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;


@RocketMQAction(value = RequestCode.GET_ALL_TOPIC_CONFIG, resource = ResourceType.TOPIC, action = Action.LIST)
public class GetAllTopicConfigResponseHeader implements CommandHeader {

    private Boolean lo;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
