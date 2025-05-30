
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class GetKVConfigResponseHeader implements CommandHeader {
    @CFNullable
    private String value;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
