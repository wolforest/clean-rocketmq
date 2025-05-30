package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.REGISTER_TOPIC_IN_NAMESRV, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class RegisterTopicRequestHeader extends RpcRequestHeader {
    @CFNotNull
    private String topic;

    protected Boolean lo;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
