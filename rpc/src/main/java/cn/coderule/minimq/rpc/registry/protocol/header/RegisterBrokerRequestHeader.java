

/**
 * $Id: RegisterBrokerRequestHeader.java 1835 2013-05-16 02:00:50Z vintagewang@apache.org $
 */package cn.coderule.minimq.rpc.registry.protocol.header;

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
@RocketMQAction(value = RequestCode.REGISTER_BROKER, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class RegisterBrokerRequestHeader implements CommandHeader {
    @CFNotNull
    private String brokerName;
    @CFNotNull
    private String brokerAddr;
    @CFNotNull
    @RocketMQResource(ResourceType.CLUSTER)
    private String clusterName;
    @CFNotNull
    private String haServerAddr;
    @CFNotNull
    private Long brokerId;
    @CFNullable
    private Long heartbeatTimeoutMillis;
    @CFNullable
    private Boolean enableActingMaster;

    private boolean compressed;

    private Integer bodyCrc32 = 0;

    @Override
    public void checkFields() throws RemotingCommandException {
    }
}
