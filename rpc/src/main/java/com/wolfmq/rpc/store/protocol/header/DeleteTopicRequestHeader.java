

/**
 * $Id: DeleteTopicRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package com.wolfmq.rpc.store.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQResource;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import lombok.Getter;
import lombok.Setter;

@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_BROKER, action = Action.DELETE)
public class DeleteTopicRequestHeader extends RpcRequestHeader {
    @Setter @Getter @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    private Boolean lo;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
