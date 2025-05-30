
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.QUERY_DATA_VERSION, resource = ResourceType.CLUSTER, action = Action.GET)
public class QueryDataVersionRequestHeader implements CommandHeader {
    @CFNotNull
    private String brokerName;
    @CFNotNull
    private String brokerAddr;
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    @CFNotNull
    private Long brokerId;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
