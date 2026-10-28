
package com.wolfmq.rpc.store.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQResource;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.RpcRequestHeader;
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
