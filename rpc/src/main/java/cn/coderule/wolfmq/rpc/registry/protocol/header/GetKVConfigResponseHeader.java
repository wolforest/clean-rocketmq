
package cn.coderule.wolfmq.rpc.registry.protocol.header;

import cn.coderule.wolfmq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.wolfmq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.wolfmq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class GetKVConfigResponseHeader implements CommandHeader {
    @CFNullable
    private String value;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
