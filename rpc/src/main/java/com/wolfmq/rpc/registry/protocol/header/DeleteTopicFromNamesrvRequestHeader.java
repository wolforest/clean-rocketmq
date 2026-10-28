package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQResource;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@RocketMQAction(value = RequestCode.DELETE_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class DeleteTopicFromNamesrvRequestHeader extends RpcRequestHeader {
    @CFNotNull
    private String topic;

    //logical
    protected Boolean lo;

    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
