
package com.wolfmq.rpc.registry.protocol.header;

import com.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import com.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import com.wolfmq.rpc.common.rpc.core.enums.Action;
import com.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import com.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import com.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import com.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.GET_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.GET)
public class GetKVConfigRequestHeader implements CommandHeader {
    @CFNotNull
    private String namespace;
    @CFNotNull
    private String key;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
