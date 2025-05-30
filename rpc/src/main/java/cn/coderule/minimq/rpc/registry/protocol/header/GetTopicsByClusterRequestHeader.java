
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;


@RocketMQAction(value = RequestCode.GET_TOPICS_BY_CLUSTER, resource = ResourceType.TOPIC, action = Action.LIST)
public class GetTopicsByClusterRequestHeader implements CommandHeader {
    @CFNotNull
    private String cluster;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }
}
