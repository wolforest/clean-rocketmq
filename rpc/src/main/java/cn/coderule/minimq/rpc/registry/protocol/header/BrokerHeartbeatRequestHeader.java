
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.BROKER_HEARTBEAT, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class BrokerHeartbeatRequestHeader implements CommandHeader {
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    @CFNotNull
    private String brokerAddr;
    @CFNotNull
    private String brokerName;
    @CFNullable
    private Long brokerId;
    @CFNullable
    private Integer epoch;
    @CFNullable
    private Long maxOffset;
    @CFNullable
    private Long confirmOffset;
    @CFNullable
    private Long heartbeatTimeoutMills;
    @CFNullable
    private Integer electionPriority;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

}
