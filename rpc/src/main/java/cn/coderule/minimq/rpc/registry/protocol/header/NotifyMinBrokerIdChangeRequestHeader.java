
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;

@RocketMQAction(value = RequestCode.NOTIFY_MIN_BROKER_ID_CHANGE, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class NotifyMinBrokerIdChangeRequestHeader implements CommandHeader {
    @CFNullable
    private Long minBrokerId;

    @CFNullable
    private String brokerName;

    @CFNullable
    private String minBrokerAddr;

    @CFNullable
    private String offlineBrokerAddr;

    @CFNullable
    private String haBrokerAddr;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    public Long getMinBrokerId() {
        return minBrokerId;
    }

    public void setMinBrokerId(Long minBrokerId) {
        this.minBrokerId = minBrokerId;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getMinBrokerAddr() {
        return minBrokerAddr;
    }

    public void setMinBrokerAddr(String minBrokerAddr) {
        this.minBrokerAddr = minBrokerAddr;
    }

    public String getOfflineBrokerAddr() {
        return offlineBrokerAddr;
    }

    public void setOfflineBrokerAddr(String offlineBrokerAddr) {
        this.offlineBrokerAddr = offlineBrokerAddr;
    }

    public String getHaBrokerAddr() {
        return haBrokerAddr;
    }

    public void setHaBrokerAddr(String haBrokerAddr) {
        this.haBrokerAddr = haBrokerAddr;
    }
}
