

/**
 * $Id: GetRouteInfoRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@RocketMQAction(value = RequestCode.GET_ROUTEINFO_BY_TOPIC, resource = ResourceType.CLUSTER, action = Action.GET)
public class GetRouteInfoRequestHeader extends RpcRequestHeader {

    @CFNotNull
    private String topic;

    protected Boolean lo;

    @CFNullable
    private Boolean acceptStandardJsonOnly;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
