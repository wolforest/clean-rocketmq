
package com.wolfmq.rpc.store.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQResource;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.RpcRequestHeader;

@RocketMQAction(value = RequestCode.DELETE_SUBSCRIPTIONGROUP, action = Action.DELETE)
public class DeleteSubscriptionGroupRequestHeader extends RpcRequestHeader {
    @CFNotNull
    @RocketMQResource(ResourceType.GROUP)
    private String groupName;

    private boolean cleanOffset = false;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isCleanOffset() {
        return cleanOffset;
    }

    public void setCleanOffset(boolean cleanOffset) {
        this.cleanOffset = cleanOffset;
    }
}
