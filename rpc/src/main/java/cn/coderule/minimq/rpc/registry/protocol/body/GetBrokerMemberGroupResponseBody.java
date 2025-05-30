
package cn.coderule.minimq.rpc.registry.protocol.body;

import cn.coderule.minimq.rpc.common.rpc.protocol.codec.RpcSerializable;

public class GetBrokerMemberGroupResponseBody extends RpcSerializable {
    // Contains the broker member info of the same broker group
    private BrokerMemberGroup brokerMemberGroup;

    public BrokerMemberGroup getBrokerMemberGroup() {
        return brokerMemberGroup;
    }

    public void setBrokerMemberGroup(final BrokerMemberGroup brokerMemberGroup) {
        this.brokerMemberGroup = brokerMemberGroup;
    }
}
