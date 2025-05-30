package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.WIPE_WRITE_PERM_OF_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class WipeWritePermOfBrokerRequestHeader implements CommandHeader {
    @CFNotNull
    private String brokerName;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
