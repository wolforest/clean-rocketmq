
package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.wolfmq.rpc.common.rpc.core.annotation.RocketMQAction;
import cn.coderule.wolfmq.rpc.common.rpc.core.enums.Action;
import cn.coderule.wolfmq.rpc.common.rpc.core.enums.ResourceType;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.code.RequestCode;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
@RocketMQAction(value = RequestCode.PUT_KV_CONFIG, resource = ResourceType.CLUSTER, action = Action.UPDATE)
public class PutKVConfigRequestHeader implements CommandHeader {
    @CFNotNull
    private String namespace;
    @CFNotNull
    private String key;
    @CFNotNull
    private String value;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
