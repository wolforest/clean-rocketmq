
package cn.coderule.minimq.rpc.registry.protocol.header;

import cn.coderule.minimq.rpc.common.rpc.core.annotation.CFNotNull;
import cn.coderule.minimq.rpc.common.rpc.core.exception.RemotingCommandException;
import cn.coderule.minimq.rpc.common.rpc.protocol.header.CommandHeader;
import lombok.Data;

@Data
public class QueryDataVersionResponseHeader implements CommandHeader {
    @CFNotNull
    private Boolean changed;

    @Override
    public void checkFields() throws RemotingCommandException {

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryDataVersionResponseHeader{");
        sb.append("changed=").append(changed);
        sb.append('}');
        return sb.toString();
    }
}
