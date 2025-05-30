
package cn.coderule.minimq.rpc.store.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.minimq.rpc.common.rpc.core.annotation.RocketMQResource;
import cn.coderule.minimq.rpc.common.rpc.core.enums.Action;
import cn.coderule.minimq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.RpcRequestHeader;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@RocketMQAction(value = RequestCode.GET_TOPIC_CONFIG, action = Action.GET)
public class GetTopicConfigRequestHeader extends RpcRequestHeader {
    @Override
    public void checkFields() throws RemotingCommandException {
    }

    @CFNotNull
    @RocketMQResource(ResourceType.TOPIC)
    private String topic;

    private Boolean lo;


}
