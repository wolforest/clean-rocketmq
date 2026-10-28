

/**
 * $Id: GetAllTopicConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package com.wolfmq.rpc.store.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQResource;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.RpcRequestHeader;

@RocketMQAction(value = RequestCode.GET_SUBSCRIPTIONGROUP_CONFIG, action = Action.GET)
public class GetSubscriptionGroupConfigRequestHeader extends RpcRequestHeader {

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String group;

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }
}
