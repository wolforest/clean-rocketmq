

/**
 * $Id: GetAllTopicConfigResponseHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.store.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.RpcRequestHeader;

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
