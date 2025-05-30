
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNullable;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class RegisterBrokerResponseHeader implements CommandHeader {
    @CFNullable
    private String haServerAddr;
    @CFNullable
    private String masterAddr;

    @Override
    public void checkFields() throws RemotingCommandException {
    }

}
